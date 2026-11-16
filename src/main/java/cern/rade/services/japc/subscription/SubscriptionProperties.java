package cern.rade.services.japc.subscription;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Subscription settings from {@code service-properties.subscription}.
 *
 * @param pingInterval       how often the server pings each client
 * @param socketIdleTimeout  a client that sends nothing, not even a pong, for this long is closed
 * @param firstUpdateTimeout how long the handshake waits for the first value
 * @param updateWaitTimeout  how long the sender waits for an update before re-checking that the connection is open
 * @param queueSize          updates queued per connection, the oldest is dropped when full
 */
@ConfigurationProperties(prefix = "service-properties.subscription")
public record SubscriptionProperties(
        @DefaultValue("5s") Duration pingInterval,
        @DefaultValue("10s") Duration socketIdleTimeout,
        @DefaultValue("5s") Duration firstUpdateTimeout,
        @DefaultValue("1s") Duration updateWaitTimeout,
        @DefaultValue("100") int queueSize) {

    public SubscriptionProperties {
        requirePositive("ping-interval", pingInterval);
        requirePositive("socket-idle-timeout", socketIdleTimeout);
        requirePositive("first-update-timeout", firstUpdateTimeout);
        requirePositive("update-wait-timeout", updateWaitTimeout);
        if (queueSize <= 0) {
            throw new IllegalArgumentException("service-properties.subscription.queue-size must be positive");
        }
    }

    private static void requirePositive(String name, Duration value) {
        if (value.isNegative() || value.isZero()) {
            throw new IllegalArgumentException("service-properties.subscription." + name + " must be positive");
        }
    }
}
