package cern.rade.services.japc.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Ends a browser login whose RBAC session was revoked or has expired, so the user has to log in again. Requests then
 * continue unauthenticated and get the usual login redirect or 401.
 */
public class InactiveSessionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (RbacAuthentication.current().filter(session -> !session.isActive()).isPresent()) {
            SecurityContextHolder.clearContext();
            HttpSession httpSession = request.getSession(false);
            if (httpSession != null) {
                httpSession.invalidate();
            }
        }
        chain.doFilter(request, response);
    }
}
