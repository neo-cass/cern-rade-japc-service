package cern.rade.services.japc.stats;

import java.util.Map;

/**
 * Requests of one RBAC user since the service started, per operation, and how many of them were rate limited.
 */
public record ClientRequests(String user, Map<Operation, Long> requests, long rateLimited) {
}
