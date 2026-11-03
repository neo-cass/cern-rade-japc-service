package cern.rade.services.japc.security;

import cern.rade.services.japc.rbac.RbacSession;
import cern.rbac.common.RbaToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Optional;

/**
 * What the official {@code cern.rbac.util.http.spring.RbacAuthenticationService} provides: reading the
 * {@code Authorization: rbac-token <Base64>} header and turning a token into Spring authorities
 * ({@code ROLE_<role>}, {@code LOCATION_<location>}). The released rbac-util is built on {@code javax.servlet}, so
 * {@link LocalRbacTokenSupport} mirrors it until a jakarta release (11.0.0) can be delegated to.
 */
public interface RbacTokenSupport {
    String ROLE_PREFIX = "ROLE_";
    String LOCATION_PREFIX = "LOCATION_";

    static String roleAuthority(String role) {
        return ROLE_PREFIX + role;
    }

    /**
     * @return the token from the request, or empty if the request has none
     */
    Optional<RbaToken> extract(HttpServletRequest request);

    Collection<? extends GrantedAuthority> authorities(RbaToken token);

    default Collection<? extends GrantedAuthority> authorities(RbacSession session) {
        return authorities(session.applicationToken());
    }
}
