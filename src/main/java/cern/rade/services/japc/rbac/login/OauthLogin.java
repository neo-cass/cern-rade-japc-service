package cern.rade.services.japc.rbac.login;

import cern.rade.services.japc.rbac.LoginType;
import cern.rade.services.japc.rbac.RbacProperties;
import cern.rade.services.japc.rbac.RbacSessionFactory;
import cern.rade.services.japc.rbac.gateway.RbacGateway;
import cern.rbac.common.MasterToken;

import java.time.Duration;

/**
 * {@code Rbac: Oauth <access token>}, a CERN SSO access token whose audience is the RBAC server. The admin panel uses
 * it after exchanging the logged-in user's SSO token.
 */
public class OauthLogin extends MasterTokenLogin {
    public static final String SCHEME = "Oauth";

    public OauthLogin(RbacGateway gateway, RbacSessionFactory sessions, RbacProperties properties) {
        super(SCHEME, LoginType.OAUTH, gateway, sessions, properties);
    }

    @Override
    protected MasterToken obtainMaster(RbacCredentials credentials, Duration lifetime) {
        return gateway.oauthMaster(credentials.value(), lifetime);
    }
}
