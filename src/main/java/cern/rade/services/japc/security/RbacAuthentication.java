package cern.rade.services.japc.security;

import cern.rade.services.japc.rbac.RbacSession;
import cern.rade.services.japc.security.sso.RbacOidcUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Optional;

/**
 * A request authenticated with the {@code Rbac} or {@code Authorization: rbac-token} header.
 */
public class RbacAuthentication extends AbstractAuthenticationToken {
    private final transient RbacSession session;

    public RbacAuthentication(RbacSession session, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.session = session;
        setAuthenticated(true);
    }

    /**
     * The RBAC session of the current request, however the client logged in.
     */
    public static Optional<RbacSession> current() {
        return of(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * The RBAC session behind an authentication, however the client logged in.
     */
    public static Optional<RbacSession> of(Authentication authentication) {
        if (authentication == null) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof RbacSession session) {
            return Optional.of(session);
        }
        if (principal instanceof RbacOidcUser ssoUser) {
            return Optional.of(ssoUser.rbacSession());
        }
        return Optional.empty();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public RbacSession getPrincipal() {
        return session;
    }

    @Override
    public String getName() {
        return session.user();
    }
}
