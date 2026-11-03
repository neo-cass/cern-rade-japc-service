package cern.rade.services.japc.rbac.login;

import cern.rade.services.japc.rbac.LoginType;
import cern.rade.services.japc.rbac.RbacProperties;
import cern.rade.services.japc.rbac.RbacSession;
import cern.rade.services.japc.rbac.RbacSessionFactory;
import cern.rade.services.japc.rbac.RbacSessionRegistry;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RbacLoginServiceTest {
    private final RbacProperties properties = new RbacProperties("USER", "ADMIN", Duration.ofHours(1), Duration.ofDays(1), Duration.ZERO);
    private final RbacSessionFactory sessions = new RbacSessionFactory(null, properties, Clock.systemUTC());
    private final RbacSessionRegistry registry = new RbacSessionRegistry(properties, Clock.systemUTC());

    @Test
    void dispatchesOnSchemeAndCachesSession() {
        RbacLoginService service = new RbacLoginService(List.of(fixedLogin("Explicit", LoginType.EXPLICIT)), registry);
        RbacSession session = service.login("Explicit dXNlcjpwYXNz");
        assertEquals(LoginType.EXPLICIT, session.loginType());
        assertSame(session, registry.find(session.id()).orElseThrow());
    }

    @Test
    void doesNotCachePassThroughTokens() {
        RbacLoginService service = new RbacLoginService(List.of(fixedLogin("Token", LoginType.TOKEN)), registry);
        RbacSession session = service.login("Token abc");
        assertTrue(registry.find(session.id()).isEmpty());
    }

    @Test
    void cachedLoginFindsRegisteredSession() {
        RbacLoginService service = new RbacLoginService(
                List.of(fixedLogin("Explicit", LoginType.EXPLICIT), new CachedLogin(registry)), registry);
        RbacSession session = service.login("Explicit dXNlcjpwYXNz");
        assertSame(session, service.login("Cached " + session.id()));
    }

    @Test
    void rejectsUnknownScheme() {
        RbacLoginService service = new RbacLoginService(List.of(fixedLogin("Explicit", LoginType.EXPLICIT)), registry);
        assertThrows(AuthenticationException.class, () -> service.login("Basic dXNlcjpwYXNz"));
    }

    @Test
    void rejectsUnknownCachedId() {
        RbacLoginService service = new RbacLoginService(List.of(new CachedLogin(registry)), registry);
        assertThrows(AuthenticationException.class, () -> service.login("Cached 00000000-0000-0000-0000-000000000000"));
    }

    private RbacLogin fixedLogin(String scheme, LoginType loginType) {
        return new RbacLogin() {
            @Override
            public String scheme() {
                return scheme;
            }

            @Override
            public RbacSession login(RbacCredentials credentials) {
                return sessions.fromToken(loginType, RbaToken.EMPTY_TOKEN);
            }
        };
    }
}
