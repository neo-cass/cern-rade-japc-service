package cern.rade.services.japc.security.sso;

import cern.rade.services.japc.rbac.RbacSession;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;

/**
 * A CERN SSO user whose authorities are the roles of the RBAC session its SSO token was exchanged for.
 */
public class RbacOidcUser extends DefaultOidcUser {
    private final transient RbacSession session;

    public RbacOidcUser(OidcUser user, RbacSession session, Collection<? extends GrantedAuthority> authorities) {
        super(authorities, user.getIdToken(), user.getUserInfo());
        this.session = session;
    }

    public RbacSession rbacSession() {
        return session;
    }
}
