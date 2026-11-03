package cern.rade.services.japc.admin.view;

import java.time.Instant;

/**
 * Version, uptime and RBAC settings of the running service. Durations are ISO-8601 strings, e.g. {@code PT12H}.
 */
public record ServiceInfoView(String version, Instant startedAt, String uptime, String rbacEnvironment,
                              String requiredRole, String adminRole, String tokenLifetime, String sessionLifetime,
                              String sessionIdleTimeout, int sessions, int subscriptions) {
}
