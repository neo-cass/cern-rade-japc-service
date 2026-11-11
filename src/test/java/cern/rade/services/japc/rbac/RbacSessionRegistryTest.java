package cern.rade.services.japc.rbac;

import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.time.Clock;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RbacSessionRegistryTest {
    private final RbacProperties properties = new RbacProperties("USER", "ADMIN", Duration.ofHours(1), Duration.ofDays(1), Duration.ZERO);
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
    void revokesAll() {
        registry.register(sessions.fromToken(LoginType.EXPLICIT, RbaToken.EMPTY_TOKEN, ClientContext.UNKNOWN));
        registry.register(sessions.fromToken(LoginType.KERBEROS, RbaToken.EMPTY_TOKEN, ClientContext.UNKNOWN));
        assertEquals(2, registry.revokeAll());
        assertTrue(registry.list().isEmpty());
    }

    @Test
    void sessionIdsAreUnguessableUuids() {
        RbacSession session = sessions.fromToken(LoginType.EXPLICIT, RbaToken.EMPTY_TOKEN, ClientContext.UNKNOWN);
        assertEquals(36, session.id().length());
        assertFalse(session.id().contains(session.handle()));
    }
}
