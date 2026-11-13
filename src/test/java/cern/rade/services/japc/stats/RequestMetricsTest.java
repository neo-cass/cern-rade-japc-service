package cern.rade.services.japc.stats;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestMetricsTest {
    private final RequestMetrics metrics = new RequestMetrics(new SimpleMeterRegistry());

    @Test
    void sumsPerUserAcrossAddressesAndStatuses() {
        long start = System.nanoTime();
        metrics.record(new Client("alice", "10.0.0.1"), Operation.GET, "/api/v1/D/P", 200, start);
        metrics.record(new Client("alice", "10.0.0.2"), Operation.GET, "/api/v1/D/P", 429, start);
        metrics.record(new Client("alice", "10.0.0.1"), Operation.SUBSCRIBE, "/ws/v1/D/P", 101, start);
        metrics.record(new Client("bob", "10.0.0.3"), Operation.SET, "/api/v1/D/P", 400, start);

        assertEquals(List.of(
                new ClientRequests("alice", Map.of(Operation.GET, 2L, Operation.SUBSCRIBE, 1L), 1),
                new ClientRequests("bob", Map.of(Operation.SET, 1L), 0)), metrics.clients());
    }
}
