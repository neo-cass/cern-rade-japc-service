package cern.rade.services.japc.security;

import cern.rade.services.japc.rbac.ClientContext;
import cern.rade.services.japc.rbac.LoginType;
import cern.rade.services.japc.rbac.RbacProperties;
import cern.rade.services.japc.rbac.RbacSession;
import cern.rade.services.japc.rbac.RbacSessionFactory;
import cern.rade.services.japc.rbac.RbacSessionRegistry;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RbacSessionEndpointTest {
    private final RbacProperties properties = new RbacProperties("USER", "ADMIN", Duration.ofHours(1), Duration.ofDays(1), Duration.ZERO, 0);
    private final RbacSessionFactory sessions = new RbacSessionFactory(null, properties, Clock.systemUTC());
    private final RbacSessionRegistry registry = new RbacSessionRegistry(properties, Clock.systemUTC());
    private final RbacSessionEndpoint endpoint = new RbacSessionEndpoint(registry);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loginHandsOutTheCachedIdAndMarksItIssued() {
        RbacSession session = authenticated(LoginType.EXPLICIT);
        registry.register(session);

        ResponseEntity<Void> response = endpoint.login();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("Cached " + session.id(), response.getHeaders().getFirst("Rbac"));
        assertEquals("no-store", response.getHeaders().getCacheControl());
        assertNull(response.getBody());
        assertTrue(session.issued());
    }

    @Test
    void loginWithATokenReturnsTheCachedId() {
        RbacSession session = authenticated(LoginType.TOKEN);

        ResponseEntity<Void> response = endpoint.login();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("Cached " + session.id(), response.getHeaders().getFirst("Rbac"));
        assertTrue(session.issued());
    }

    @Test
    void logoutRevokesOnlyTheCallersSession() {
        RbacSession own = registry.register(sessions.fromToken(LoginType.EXPLICIT, RbaToken.EMPTY_TOKEN, ClientContext.UNKNOWN));
        RbacSession other = registry.register(sessions.fromToken(LoginType.EXPLICIT, RbaToken.EMPTY_TOKEN, ClientContext.UNKNOWN));
        authenticate(own);

        assertEquals(HttpStatus.NO_CONTENT, endpoint.logout().getStatusCode());

        assertTrue(registry.find(own.id()).isEmpty());
        assertFalse(own.isActive());
        assertTrue(registry.find(other.id()).isPresent());
    }

    @Test
    void rejectsCallsWithoutASession() {
        assertThrows(AuthenticationException.class, endpoint::login);
        assertThrows(AuthenticationException.class, endpoint::info);
        assertThrows(AuthenticationException.class, endpoint::logout);
    }

    private RbacSession authenticated(LoginType loginType) {
        RbacSession session = sessions.fromToken(loginType, RbaToken.EMPTY_TOKEN, ClientContext.UNKNOWN);
        authenticate(session);
        return session;
    }

    private static void authenticate(RbacSession session) {
        SecurityContextHolder.getContext().setAuthentication(new RbacAuthentication(session, List.of()));
    }
}
