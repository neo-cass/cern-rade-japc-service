package cern.rade.services.japc.stats;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Counts every GET, SET and subscription handshake as {@code japc.requests} (tags operation, user, status) and logs it.
 * The client address and target only go to the log: as metric tags they would create one time series per value.
 */
@Component
public class RequestMetrics {
    private static final String REQUESTS = "japc.requests";
    private static final String RATE_LIMITED = String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value());
    private final Logger logger = LoggerFactory.getLogger(RequestMetrics.class);
    private final MeterRegistry registry;

    public RequestMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void record(Client client, Operation operation, String target, int status, long startNanos) {
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        registry.counter(REQUESTS, "operation", operation.name(), "user", client.user(), "status", String.valueOf(status)).increment();
        // Successful GETs are most of the traffic and already counted above: DEBUG keeps them out of MONIT unless this
        // logger is set to DEBUG (LOGGING_LEVEL_CERN_RADE_SERVICES_JAPC_STATS_REQUESTMETRICS=DEBUG).
        Level level = operation == Operation.GET && status < 400 ? Level.DEBUG : Level.INFO;
        // key-values become attributes of the OTLP log record in MONIT OpenSearch
        logger.atLevel(level).addKeyValue("user", client.user()).addKeyValue("address", client.address())
                .addKeyValue("operation", operation).addKeyValue("target", target)
                .addKeyValue("status", status).addKeyValue("durationMs", durationMs)
                .log("{} {} by {} from {}: {} in {} ms", operation, target, client.user(), client.address(), status, durationMs);
    }

    public List<ClientRequests> clients() {
        Map<String, List<Counter>> byUser = registry.find(REQUESTS).counters().stream()
                .collect(Collectors.groupingBy(counter -> counter.getId().getTag("user")));
        return byUser.entrySet().stream().map(entry -> {
            Map<Operation, Long> requests = new EnumMap<>(Operation.class);
            long rateLimited = 0;
            for (Counter counter : entry.getValue()) {
                long count = (long) counter.count();
                requests.merge(Operation.valueOf(counter.getId().getTag("operation")), count, Long::sum);
                rateLimited += RATE_LIMITED.equals(counter.getId().getTag("status")) ? count : 0;
            }
            return new ClientRequests(entry.getKey(), requests, rateLimited);
        }).sorted(Comparator.comparing(ClientRequests::user)).toList();
    }
}
