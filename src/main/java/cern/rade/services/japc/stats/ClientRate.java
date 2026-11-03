package cern.rade.services.japc.stats;

import java.time.Instant;
import java.util.Map;

/**
 * Request rates of one client.
 *
 * @param operations per operation: requests in the last minute and since the service started
 */
public record ClientRate(String user, String address, Instant lastSeen, Map<Operation, Count> operations) {

    public record Count(long lastMinute, long total) {
    }
}
