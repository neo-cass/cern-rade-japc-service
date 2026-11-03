package cern.rade.services.japc.rbac;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RbacPropertiesTest {

    @Test
    void capsLifetimesAtRbacMaxima() {
        RbacProperties properties = new RbacProperties("USER", "ADMIN", Duration.ofHours(24), Duration.ofDays(500), Duration.ofHours(1));
        assertEquals(RbacProperties.MAX_TOKEN_LIFETIME, properties.tokenLifetime());
        assertEquals(RbacProperties.MAX_SESSION_LIFETIME, properties.sessionLifetime());
    }

    @Test
    void keepsLifetimesWithinMaxima() {
        RbacProperties properties = new RbacProperties("USER", "ADMIN", Duration.ofHours(1), Duration.ofDays(7), Duration.ofHours(1));
        assertEquals(Duration.ofHours(1), properties.tokenLifetime());
        assertEquals(Duration.ofDays(7), properties.sessionLifetime());
    }

    @Test
    void zeroIdleTimeoutDisablesIt() {
        assertFalse(new RbacProperties("USER", "ADMIN", Duration.ofHours(1), Duration.ofDays(1), Duration.ZERO).idleTimeoutEnabled());
        assertTrue(new RbacProperties("USER", "ADMIN", Duration.ofHours(1), Duration.ofDays(1), Duration.ofHours(1)).idleTimeoutEnabled());
    }

    @Test
    void requiresRoles() {
        assertThrows(NullPointerException.class,
                () -> new RbacProperties(null, "ADMIN", Duration.ofHours(1), Duration.ofDays(1), Duration.ZERO));
    }
}
