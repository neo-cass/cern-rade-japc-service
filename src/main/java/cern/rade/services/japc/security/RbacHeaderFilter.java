package cern.rade.services.japc.security;

import cern.rade.services.japc.error.ErrorCode;
import cern.rade.services.japc.rbac.LoginType;
import cern.rade.services.japc.rbac.RbacHeaders;
import cern.rade.services.japc.rbac.RbacSession;
import cern.rade.services.japc.rbac.RbacSessionFactory;
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
 * Requests without either pass through unauthenticated; rejected credentials get a 401.
 */
public class RbacHeaderFilter extends OncePerRequestFilter {
    private final RbacLoginService loginService;
    private final RbacTokenSupport tokenSupport;
    private final RbacSessionFactory sessions;

    public RbacHeaderFilter(RbacLoginService loginService, RbacTokenSupport tokenSupport, RbacSessionFactory sessions) {
        this.loginService = loginService;
        this.tokenSupport = tokenSupport;
        this.sessions = sessions;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            session(request).ifPresent(session -> SecurityContextHolder.getContext()
                    .setAuthentication(new RbacAuthentication(session, tokenSupport.authorities(session))));
        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            SecurityErrorWriter.write(request, response, HttpStatus.UNAUTHORIZED, ErrorCode.message(e));
            return;
        }
        chain.doFilter(request, response);
    }

    private Optional<RbacSession> session(HttpServletRequest request) {
        String header = request.getHeader(RbacHeaders.HEADER);
        if (header != null) {
            return Optional.of(loginService.login(header));
        }
        return tokenSupport.extract(request).map(token -> sessions.fromToken(LoginType.TOKEN, token));
    }
}
