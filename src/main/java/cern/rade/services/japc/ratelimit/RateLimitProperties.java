package cern.rade.services.japc.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Map;

/**
 * Rate limits from {@code service-properties.rate-limit}, per RBAC user. Zero disables a limit.
 *
 * @param requestsPerSecond       GET, SET and subscription handshakes together
 * @param burst                   requests allowed at once before the per-second rate applies
 * @param subscriptionsPerUser    most open subscriptions per user
 * @param requestsPerSecondByUser overrides of {@code requestsPerSecond}, e.g. for accounts shared by many machines
 */
@ConfigurationProperties(prefix = "service-properties.rate-limit")
public record RateLimitProperties(
        @DefaultValue("50") int requestsPerSecond,
        @DefaultValue("100") int burst,
        @DefaultValue("200") int subscriptionsPerUser,
        @DefaultValue Map<String, Integer> requestsPerSecondByUser) {
}
