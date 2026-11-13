package cern.rade.services.japc.rbac;

import cern.rade.services.japc.MutableClock;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RbacSessionRegistryTest {
    private final RbacProperties properties = new RbacProperties("USER", "ADMIN", Duration.ofHours(1), Duration.ofDays(1), Duration.ZERO, 0);
    private final RbacSessionFactory sessions = new RbacSessionFactory(null, properties, Clock.systemUTC());
    private final RbacSessionRegistry registry = new RbacSessionRegistry(properties, Clock.systemUTC());

    @Test
    void revokesByPublicHandleNotSecretId() {
        RbacSession session = registry.register(sessions.fromToken(LoginType.EXPLICIT, RbaToken.EMPTY_TOKEN, ClientContext.UNKNOWN));
        assertFalse(registry.revoke(session.id()));
        assertTrue(registry.revoke(session.handle()));
        assertTrue(registry.find(session.id()).isEmpty());
    }

    @Test
    void revokedSessionRefusesTokensEvenWhenStillReferenced() {
        RbacSession session = registry.register(sessions.fromToken(LoginType.OAUTH, RbaToken.EMPTY_TOKEN, ClientContext.UNKNOWN));
        registry.revokeAll();
        assertFalse(session.isActive());
        assertThrows(AuthenticationException.class, session::applicationToken);
    }

    @Test
    void keepsOnlySessionsWhoseCachedIdWasSent() {
        RbacSession sent = registry.register(sessions.fromToken(LoginType.EXPLICIT, RbaToken.EMPTY_TOKEN, ClientContext.UNKNOWN));
        RbacSession notSent = registry.register(sessions.fromToken(LoginType.EXPLICIT, RbaToken.EMPTY_TOKEN, ClientContext.UNKNOWN));
        RbacHeaders.setCached(new HttpHeaders(), sent);
        registry.releaseIfUnissued(sent);
        registry.releaseIfUnissued(notSent);
        assertTrue(registry.find(sent.id()).isPresent());
        assertTrue(registry.find(notSent.id()).isEmpty());
    }

    @Test
    void revokesTheSessionOfTheCaller() {
        RbacSession own = registry.register(sessions.fromToken(LoginType.EXPLICIT, RbaToken.EMPTY_TOKEN, ClientContext.UNKNOWN));
        RbacSession other = registry.register(sessions.fromToken(LoginType.EXPLICIT, RbaToken.EMPTY_TOKEN, ClientContext.UNKNOWN));
        registry.revoke(own);
        assertTrue(registry.find(own.id()).isEmpty());
        assertFalse(own.isActive());
        assertTrue(registry.find(other.id()).isPresent());
    }

    @Test
    void revokesAll() {
        registry.register(sessions.fromToken(LoginType.EXPLICIT, RbaToken.EMPTY_TOKEN, ClientContext.UNKNOWN));
        registry.register(sessions.fromToken(LoginType.KERBEROS, RbaToken.EMPTY_TOKEN, ClientContext.UNKNOWN));
        assertEquals(2, registry.revokeAll());
        assertTrue(registry.list().isEmpty());
    }

    @Test
    void revokesLeastRecentlyUsedSessionsAboveTheUserLimit() {
        MutableClock clock = new MutableClock(Instant.parse("2026-11-10T12:00:00Z"));
        RbacSessionRegistry limited = new RbacSessionRegistry(
                new RbacProperties("USER", "ADMIN", Duration.ofHours(1), Duration.ofDays(1), Duration.ZERO, 2), clock);
        RbacSession oldest = limited.register(sessionOf("alice", clock));
        clock.advance(Duration.ofMinutes(1));
        RbacSession middle = limited.register(sessionOf("alice", clock));
        clock.advance(Duration.ofMinutes(1));
        RbacSession other = limited.register(sessionOf("bob", clock));
        RbacSession newest = limited.register(sessionOf("alice", clock));

        assertTrue(limited.find(oldest.id()).isEmpty());
        assertFalse(oldest.isActive());
        assertEquals(List.of(middle, newest), limited.sessionsOf("alice").stream()
                .sorted(Comparator.comparing(RbacSession::lastUsed)).toList());
        assertTrue(limited.find(other.id()).isPresent());
    }

    @Test
    void revokesAllSessionsOfOneUser() {
        MutableClock clock = new MutableClock(Instant.parse("2026-11-10T12:00:00Z"));
        RbacSession alice1 = registry.register(sessionOf("alice", clock));
        RbacSession alice2 = registry.register(sessionOf("alice", clock));
        RbacSession bob = registry.register(sessionOf("bob", clock));
        assertEquals(2, registry.revokeUser("alice"));
        assertTrue(registry.find(alice1.id()).isEmpty());
        assertTrue(registry.find(alice2.id()).isEmpty());
        assertTrue(registry.find(bob.id()).isPresent());
    }

    @Test
    void sessionIdsAreUnguessableUuids() {
        RbacSession session = sessions.fromToken(LoginType.EXPLICIT, RbaToken.EMPTY_TOKEN, ClientContext.UNKNOWN);
        assertEquals(36, session.id().length());
        assertFalse(session.id().contains(session.handle()));
    }

    /**
     * A session for a user without a real signed token, which tests cannot create.
     */
    private static RbacSession sessionOf(String user, Clock clock) {
        return new RbacSession(LoginType.EXPLICIT, RbaToken.EMPTY_TOKEN, null, null, ClientContext.UNKNOWN, clock) {
            @Override
            public String user() {
                return user;
            }
        };
    }
}
