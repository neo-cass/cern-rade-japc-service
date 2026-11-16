package cern.rade.services.japc.subscription;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Subscription settings must be positive, and a rejected one is named in the error.
 */
class SubscriptionPropertiesTest {
    private static final Duration OK = Duration.ofSeconds(1);

    private static void assertRejected(String property, Runnable create) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, create::run);
        assertTrue(error.getMessage().contains(property), error.getMessage());
    }

    @Test
    void zeroOrNegativeDurationsAreRejected() {
        for (Duration bad : new Duration[]{Duration.ZERO, Duration.ofSeconds(-1)}) {
            assertRejected("ping-interval", () -> new SubscriptionProperties(bad, OK, OK, OK, 1));
            assertRejected("socket-idle-timeout", () -> new SubscriptionProperties(OK, bad, OK, OK, 1));
            assertRejected("first-update-timeout", () -> new SubscriptionProperties(OK, OK, bad, OK, 1));
            assertRejected("update-wait-timeout", () -> new SubscriptionProperties(OK, OK, OK, bad, 1));
        }
    }

    @Test
    void queueSizeMustBePositive() {
        assertRejected("queue-size", () -> new SubscriptionProperties(OK, OK, OK, OK, 0));
        assertRejected("queue-size", () -> new SubscriptionProperties(OK, OK, OK, OK, -5));
    }

    @Test
    void validValuesAreKept() {
        SubscriptionProperties properties = new SubscriptionProperties(Duration.ofSeconds(1), Duration.ofSeconds(2),
                Duration.ofSeconds(3), Duration.ofSeconds(4), 5);
        assertEquals(Duration.ofSeconds(1), properties.pingInterval());
        assertEquals(Duration.ofSeconds(2), properties.socketIdleTimeout());
        assertEquals(Duration.ofSeconds(3), properties.firstUpdateTimeout());
        assertEquals(Duration.ofSeconds(4), properties.updateWaitTimeout());
        assertEquals(5, properties.queueSize());
    }
}
