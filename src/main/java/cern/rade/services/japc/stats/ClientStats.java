package cern.rade.services.japc.stats;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GET, SET and subscription counts per client, for the admin panel.
 */
public class ClientStats {
    private final Map<Client, Counters> clients = new ConcurrentHashMap<>();
    private final Clock clock;

    public ClientStats(Clock clock) {
        this.clock = clock;
    }

    public void record(Client client, Operation operation) {
        clients.computeIfAbsent(client, key -> new Counters()).record(operation, clock.instant());
    }

    public List<ClientRate> rates() {
        long now = clock.instant().getEpochSecond();
        return clients.entrySet().stream()
                .map(entry -> entry.getValue().rate(entry.getKey(), now))
                .sorted(Comparator.comparing(ClientRate::lastSeen).reversed())
                .toList();
    }

    private static final class Counters {
        private final Map<Operation, RateWindow> windows = new EnumMap<>(Operation.class);
        private volatile Instant lastSeen;

        Counters() {
            for (Operation operation : Operation.values()) {
                windows.put(operation, new RateWindow());
            }
        }

        void record(Operation operation, Instant at) {
            windows.get(operation).record(at.getEpochSecond());
            lastSeen = at;
        }

        ClientRate rate(Client client, long now) {
            Map<Operation, ClientRate.Count> counts = new EnumMap<>(Operation.class);
            windows.forEach((operation, window) -> counts.put(operation, new ClientRate.Count(window.lastMinute(now), window.total())));
            return new ClientRate(client.user(), client.address(), lastSeen, counts);
        }
    }
}
