package cern.rade.services.japc.rbac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.Objects;

/**
 * RBAC settings from {@code service-properties.rbac}. Lifetimes default to, and are capped at, the maxima the RBAC
 * server allows: 12 hours for application tokens and one year for master tokens.
 *
 * @param requiredRole       RBAC role needed to use the API and subscriptions
 * @param adminRole          RBAC role needed to use the admin panel and admin API
 * @param tokenLifetime      lifetime requested for application tokens; also the delay before a removed role takes effect
 * @param sessionLifetime    lifetime requested for master tokens, i.e. how long a cached session can be renewed
 * @param sessionIdleTimeout cached sessions unused for this long are dropped; zero disables it
 */
@ConfigurationProperties(prefix = "service-properties.rbac")
public record RbacProperties(
        String requiredRole,
        String adminRole,
        @DefaultValue("12h") Duration tokenLifetime,
        @DefaultValue("365d") Duration sessionLifetime,
        @DefaultValue("24h") Duration sessionIdleTimeout) {

    public static final Duration MAX_TOKEN_LIFETIME = Duration.ofHours(12);
    public static final Duration MAX_SESSION_LIFETIME = Duration.ofDays(365);
    private static final Logger logger = LoggerFactory.getLogger(RbacProperties.class);

    public RbacProperties {
        Objects.requireNonNull(requiredRole, "service-properties.rbac.required-role must be set");
        Objects.requireNonNull(adminRole, "service-properties.rbac.admin-role must be set");
        tokenLifetime = capped("token-lifetime", tokenLifetime, MAX_TOKEN_LIFETIME);
        sessionLifetime = capped("session-lifetime", sessionLifetime, MAX_SESSION_LIFETIME);
    }

    public boolean idleTimeoutEnabled() {
        return !sessionIdleTimeout.isZero();
    }

    private static Duration capped(String name, Duration requested, Duration maximum) {
        if (requested.compareTo(maximum) <= 0) {
            return requested;
        }
        logger.warn("service-properties.rbac.{} = {} exceeds the RBAC maximum, using {}", name, requested, maximum);
        return maximum;
    }
}
