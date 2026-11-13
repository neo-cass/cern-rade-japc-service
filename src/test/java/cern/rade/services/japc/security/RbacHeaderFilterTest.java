package cern.rade.services.japc.security;

import cern.rade.services.japc.rbac.ClientContext;
import cern.rade.services.japc.rbac.LoginType;
import cern.rade.services.japc.rbac.RbacHeaders;
import cern.rade.services.japc.rbac.RbacProperties;
import cern.rade.services.japc.rbac.RbacSession;
import cern.rade.services.japc.rbac.RbacSessionFactory;
import cern.rade.services.japc.rbac.RbacSessionRegistry;
import cern.rade.services.japc.rbac.login.CachedLogin;
import cern.rade.services.japc.rbac.login.RbacCredentials;
import cern.rade.services.japc.rbac.login.RbacLogin;
import cern.rade.services.japc.rbac.login.RbacLoginService;
import cern.rbac.common.RbaToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The filter against stub servlet objects, so no application context or RBAC server is needed.
 */
class RbacHeaderFilterTest {
    private static final RequestMatcher PUT_SESSION = request -> "PUT".equals(request.getMethod()) && "/rbac/session".equals(request.getRequestURI());
    private final RbacProperties properties = new RbacProperties("USER", "ADMIN", Duration.ofHours(1), Duration.ofDays(1), Duration.ZERO, 0);
    private final RbacSessionFactory sessions = new RbacSessionFactory(null, properties, Clock.systemUTC());
    private final RbacSessionRegistry registry = new RbacSessionRegistry(properties, Clock.systemUTC());
    private final RbacTokenSupport tokenSupport = new RbacTokenSupport() {
        @Override
        public Collection<? extends GrantedAuthority> authorities(RbaToken token) {
            return List.of();
        }

        @Override
        public Collection<? extends GrantedAuthority> authorities(RbacSession session) {
            return List.of();
        }
    };
    private final RbacLoginService loginService = new RbacLoginService(
            List.of(new FixedLogin("Explicit", LoginType.EXPLICIT, true), new FixedLogin("Token", LoginType.TOKEN, false), new CachedLogin(registry)),
            registry);
    private final RbacHeaderFilter filter = new RbacHeaderFilter(loginService, tokenSupport, registry, PUT_SESSION);
    private final AtomicReference<RbacSession> seenByChain = new AtomicReference<>();
    private final FilterChain chain = (request, response) -> seenByChain.set(RbacAuthentication.current().orElse(null));

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loginSchemeIsRejectedOutsideTheLoginRequest() throws Exception {
        for (String[] call : new String[][]{{"GET", "/api/v1/DEV/PROP"}, {"PUT", "/api/v1/DEV/PROP"}, {"GET", "/ws/v1/DEV/PROP"},
                {"GET", "/admin/api/info"}, {"GET", "/rbac/session"}, {"DELETE", "/rbac/session"}}) {
            StubResponse response = filter(call[0], call[1], "Explicit abc");
            assertEquals(401, response.status, call[0] + " " + call[1]);
            assertTrue(response.body().contains("PUT /rbac/session"), response.body());
        }
        assertNull(seenByChain.get());
        assertTrue(registry.list().isEmpty());
    }

    @Test
    void loginRequestLogsInAndKeepsTheSessionOnceItsIdWasIssued() throws Exception {
        CapturingChain issuing = new CapturingChain(session -> RbacHeaders.setCached(new HttpHeaders(), session));
        filter.doFilterInternal(request("PUT", "/rbac/session", "Explicit abc"), new StubResponse().proxy(), issuing);
        assertEquals(1, registry.list().size());
        assertSame(registry.list().get(0), issuing.session);
    }

    @Test
    void loginSessionWhoseIdWasNotIssuedIsReleased() throws Exception {
        StubResponse response = filter("PUT", "/rbac/session", "Explicit abc");
        assertEquals(200, response.status);
        assertTrue(seenByChain.get() != null, "the chain runs authenticated");
        assertTrue(registry.list().isEmpty());
        assertFalse(seenByChain.get().isActive());
    }

    @Test
    void cachedSessionAuthenticatesEverywhereAndIsNotReleased() throws Exception {
        RbacSession session = registry.register(sessions.fromToken(LoginType.EXPLICIT, RbaToken.EMPTY_TOKEN, ClientContext.UNKNOWN));
        for (String[] call : new String[][]{{"GET", "/api/v1/DEV/PROP"}, {"PUT", "/rbac/session"}, {"GET", "/admin/api/info"}}) {
            seenByChain.set(null);
            assertEquals(200, filter(call[0], call[1], "Cached " + session.id()).status);
            assertSame(session, seenByChain.get());
            assertTrue(registry.find(session.id()).isPresent());
        }
    }

    @Test
    void tokenAuthenticatesEverywhereWithoutCaching() throws Exception {
        for (String[] call : new String[][]{{"GET", "/api/v1/DEV/PROP"}, {"PUT", "/rbac/session"}}) {
            seenByChain.set(null);
            assertEquals(200, filter(call[0], call[1], "Token abc").status);
            assertEquals(LoginType.TOKEN, seenByChain.get().loginType());
        }
        assertTrue(registry.list().isEmpty());
    }

    @Test
    void unknownCachedIdAndSchemeAreRejected() throws Exception {
        assertEquals(401, filter("GET", "/api/v1/DEV/PROP", "Cached 00000000-0000-0000-0000-000000000000").status);
        assertEquals(401, filter("PUT", "/rbac/session", "Unknown abc").status);
        assertEquals(401, filter("GET", "/api/v1/DEV/PROP", "Explicit").status);
        assertNull(seenByChain.get());
    }

    @Test
    void requestsWithoutTheHeaderPassThroughUnauthenticated() throws Exception {
        StubResponse response = filter("GET", "/api/v1/DEV/PROP", null);
        assertEquals(200, response.status);
        assertNull(seenByChain.get());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private StubResponse filter(String method, String path, String rbac) throws Exception {
        StubResponse response = new StubResponse();
        filter.doFilterInternal(request(method, path, rbac), response.proxy(), chain);
        return response;
    }

    private static HttpServletRequest request(String method, String path, String rbac) {
        return (HttpServletRequest) Proxy.newProxyInstance(RbacHeaderFilterTest.class.getClassLoader(), new Class<?>[]{HttpServletRequest.class},
                (proxy, call, args) -> switch (call.getName()) {
                    case "getMethod" -> method;
                    case "getRequestURI" -> path;
                    case "getRemoteAddr" -> "127.0.0.1";
                    case "getHeader" -> "Rbac".equals(args[0]) ? rbac : null;
                    default -> defaultValue(call.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class) {
            return 0;
        }
        return type == long.class ? (Object) 0L : null;
    }

    /**
     * Records the status and body of the 401 the filter writes itself.
     */
    private static final class StubResponse {
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private int status = 200;

        HttpServletResponse proxy() {
            ServletOutputStream out = new ServletOutputStream() {
                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(WriteListener listener) {
                }

                @Override
                public void write(int b) {
                    bytes.write(b);
                }
            };
            return (HttpServletResponse) Proxy.newProxyInstance(RbacHeaderFilterTest.class.getClassLoader(), new Class<?>[]{HttpServletResponse.class},
                    (proxy, call, args) -> {
                        switch (call.getName()) {
                            case "setStatus" -> status = (Integer) args[0];
                            case "getOutputStream" -> {
                                return out;
                            }
                            default -> {
                            }
                        }
                        return defaultValue(call.getReturnType());
                    });
        }

        String body() {
            return bytes.toString();
        }
    }

    /**
     * Remembers the session the filter authenticated and lets the test act on it like an endpoint would.
     */
    private static final class CapturingChain implements FilterChain {
        private final Consumer<RbacSession> action;
        private RbacSession session;

        CapturingChain(Consumer<RbacSession> action) {
            this.action = action;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            session = RbacAuthentication.current().orElseThrow();
            action.accept(session);
        }
    }

    /**
     * A scheme that hands out a fresh session without contacting RBAC, registered like the real logins do.
     */
    private final class FixedLogin implements RbacLogin {
        private final String scheme;
        private final LoginType loginType;
        private final boolean performsLogin;

        FixedLogin(String scheme, LoginType loginType, boolean performsLogin) {
            this.scheme = scheme;
            this.loginType = loginType;
            this.performsLogin = performsLogin;
        }

        @Override
        public String scheme() {
            return scheme;
        }

        @Override
        public boolean performsLogin() {
            return performsLogin;
        }

        @Override
        public RbacSession login(RbacCredentials credentials) {
            return sessions.fromToken(loginType, RbaToken.EMPTY_TOKEN, credentials.client());
        }
    }
}
