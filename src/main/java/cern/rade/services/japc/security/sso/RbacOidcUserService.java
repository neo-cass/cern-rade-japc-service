package cern.rade.services.japc.security.sso;

import cern.rade.services.japc.rbac.RbacSession;
import cern.rade.services.japc.rbac.gateway.RbacGateway;
import cern.rade.services.japc.rbac.login.OauthLogin;
import cern.rade.services.japc.rbac.login.RbacCredentials;
import cern.rade.services.japc.rbac.login.RbacLoginService;
import cern.rade.services.japc.security.RbacTokenSupport;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * After the CERN SSO login, exchanges the user's token for one the RBAC server accepts and logs in to RBAC with it,
 * so the admin panel checks the same RBAC roles as the API.
 */
public class RbacOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {
    private final OidcUserService delegate = new OidcUserService();
    private final RbacGateway gateway;
    private final RbacLoginService loginService;
    private final RbacTokenSupport tokenSupport;

    public RbacOidcUserService(RbacGateway gateway, RbacLoginService loginService, RbacTokenSupport tokenSupport) {
        this.gateway = gateway;
        this.loginService = loginService;
        this.tokenSupport = tokenSupport;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest request) {
        OidcUser user = delegate.loadUser(request);
        ClientRegistration registration = request.getClientRegistration();
        try {
            String rbacToken = gateway.exchangeForRbac(registration.getClientId(), registration.getClientSecret(),
                    request.getAccessToken().getTokenValue());
            RbacSession session = loginService.login(new RbacCredentials(OauthLogin.SCHEME, rbacToken));
            return new RbacOidcUser(user, session, tokenSupport.authorities(session));
        } catch (RuntimeException e) {
            throw new OAuth2AuthenticationException(new OAuth2Error("rbac_login_failed", "RBAC login failed: " + e.getMessage(), null), e);
        }
    }
}
