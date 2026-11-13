package cern.rade.services.japc.security;

import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.error.ErrorCode;
import cern.rade.services.japc.rbac.ClientContext;
import cern.rade.services.japc.rbac.RbacHeaders;
import cern.rade.services.japc.rbac.RbacSession;
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
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authenticates requests carrying {@code Rbac: <scheme> <value>}. Requests without the header pass through
 * unauthenticated; rejected credentials get a 401. Schemes that log in with credentials (Explicit, Kerberos, Oauth) are
 * only accepted on the login request, {@code PUT /rbac/session}; everywhere else the header must present an existing
 * session or token ({@code Cached}, {@code Token}). A session logged in for a request whose response did not hand out
 * its cached id (missing role, ...) is dropped afterwards.
 */
public class RbacHeaderFilter extends OncePerRequestFilter {
    private final RbacLoginService loginService;
    private final RbacSessionRegistry registry;
    private final RbacTokenSupport tokenSupport;
    private final RequestMatcher loginRequest;

    /**
     * @param loginRequest matches the requests allowed to log in with credentials
     */
    public RbacHeaderFilter(RbacLoginService loginService, RbacTokenSupport tokenSupport, RbacSessionRegistry registry,
                            RequestMatcher loginRequest) {
        this.loginService = loginService;
        this.tokenSupport = tokenSupport;
        this.registry = registry;
        this.loginRequest = loginRequest;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(RbacHeaders.HEADER);
        if (header == null) {
            chain.doFilter(request, response);
            return;
        }
        boolean loggingIn;
        RbacSession session;
        RbacAuthentication authentication;
        try {
            RbacCredentials credentials = RbacCredentials.parse(header, ClientContext.of(request));
            loggingIn = loginService.performsLogin(credentials.scheme());
            if (loggingIn && !loginRequest.matches(request)) {
                throw new AuthenticationException("Rbac " + credentials.scheme() + " is only accepted by PUT " + ApiPaths.RBAC_SESSION
                        + ". Log in there, then send Rbac: Cached <id>");
            }
            session = loginService.login(credentials);
            authentication = new RbacAuthentication(session, tokenSupport.authorities(session));
        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            SecurityErrorWriter.write(request, response, HttpStatus.UNAUTHORIZED, ErrorCode.message(e));
            return;
        }
        try {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } finally {
            if (loggingIn) {
                registry.releaseIfUnissued(session);
            }
        }
    }
}
