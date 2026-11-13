package cern.rade.services.japc.security;

import cern.rade.services.japc.rbac.RbacSession;
import cern.rbac.common.RbaToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * What the official {@code cern.rbac.util.http.spring.RbacAuthenticationService} provides and this service still
 * needs: turning a token into Spring authorities ({@code ROLE_<role>}, {@code LOCATION_<location>}). The released
 * rbac-util is built on {@code javax.servlet}, so {@link LocalRbacTokenSupport} mirrors it until a jakarta release
 * (11.0.0) can be delegated to.
 */
public interface RbacTokenSupport {
    String ROLE_PREFIX = "ROLE_";
    String LOCATION_PREFIX = "LOCATION_";

    static String roleAuthority(String role) {
        return ROLE_PREFIX + role;
    }

    Collection<? extends GrantedAuthority> authorities(RbaToken token);

    default Collection<? extends GrantedAuthority> authorities(RbacSession session) {
        return authorities(session.applicationToken());
    }
}
