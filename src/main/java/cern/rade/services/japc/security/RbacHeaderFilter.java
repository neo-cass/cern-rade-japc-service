package cern.rade.services.japc.security;

import cern.rade.services.japc.error.ErrorCode;
import cern.rade.services.japc.rbac.ClientContext;
import cern.rade.services.japc.rbac.LoginType;
import cern.rade.services.japc.rbac.RbacHeaders;
import cern.rade.services.japc.rbac.RbacSession;
import cern.rade.services.japc.rbac.RbacSessionFactory;
import cern.rade.services.japc.rbac.RbacSessionRegistry;
import cern.rade.services.japc.rbac.login.RbacCredentials;
import cern.rade.services.japc.rbac.login.RbacLoginService;
import cern.rbac.client.authentication.AuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Authenticates requests carrying {@code Rbac: <scheme> <value>} or {@code Authorization: rbac-token <Base64>}.
 * Requests without either pass through unauthenticated; rejected credentials get a 401. A session logged in for a
 * request whose response did not hand out its cached id (missing role, failed request, ...) is dropped afterwards.
 */
public class RbacHeaderFilter extends OncePerRequestFilter {
    private final RbacLoginService loginService;
    private final RbacTokenSupport tokenSupport;
    private final RbacSessionFactory sessions;
    private final RbacSessionRegistry registry;

    public RbacHeaderFilter(RbacLoginService loginService, RbacTokenSupport tokenSupport, RbacSessionFactory sessions,
                            RbacSessionRegistry registry) {
        this.loginService = loginService;
        this.tokenSupport = tokenSupport;
        this.sessions = sessions;
        this.registry = registry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Login login;
        RbacAuthentication authentication;
        try {
            login = login(request);
            authentication = login == null ? null : new RbacAuthentication(login.session(), tokenSupport.authorities(login.session()));
        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            SecurityErrorWriter.write(request, response, HttpStatus.UNAUTHORIZED, ErrorCode.message(e));
            return;
        }
        if (login == null) {
            chain.doFilter(request, response);
            return;
        }
        try {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } finally {
            if (login.fresh()) {
                registry.releaseIfUnissued(login.session());
            }
        }
    }

    /**
     * @return the login of this request, or null if it carries no RBAC credentials
     */
    private Login login(HttpServletRequest request) {
        String header = request.getHeader(RbacHeaders.HEADER);
        ClientContext client = ClientContext.of(request);
        if (header != null) {
            RbacCredentials credentials = RbacCredentials.parse(header, client);
            return new Login(loginService.login(credentials), !RbacHeaders.CACHED.equals(credentials.scheme()));
        }
        Optional<RbacSession> token = tokenSupport.extract(request).map(rbaToken -> sessions.fromToken(LoginType.TOKEN, rbaToken, client));
        return token.map(session -> new Login(session, false)).orElse(null);
    }

    /**
     * @param fresh true if the session was created for this request rather than reused from the cache
     */
    private record Login(RbacSession session, boolean fresh) {
    }
}
