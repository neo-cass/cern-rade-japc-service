package cern.rade.services.japc.rbac.login;

import cern.rade.services.japc.rbac.LoginType;
import cern.rade.services.japc.rbac.RbacSession;
import cern.rade.services.japc.rbac.RbacSessionFactory;
import cern.rade.services.japc.rbac.gateway.RbacGateway;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;

/**
 * {@code Rbac: Token <base64url(token)>}, an RBAC token the client obtained on its own machine. It is validated on
 * every request and used as-is until it expires, because RBAC only renews a token from the address it was issued to.
 * Nothing is cached, so there is no {@code Cached} id to hand out.
 */
public class TokenLogin implements RbacLogin {
    public static final String SCHEME = "Token";
    private final RbacGateway gateway;
    private final RbacSessionFactory sessions;

    public TokenLogin(RbacGateway gateway, RbacSessionFactory sessions) {
        this.gateway = gateway;
        this.sessions = sessions;
    }

    @Override
    public String scheme() {
        return SCHEME;
    }

    @Override
    public boolean performsLogin() {
        return false;
    }

    @Override
    public RbacSession login(RbacCredentials credentials) {
        RbaToken token = gateway.validate(credentials.decodedValue());
        if (!token.isValid()) {
            throw new AuthenticationException("RBAC token has expired. Please obtain a new one");
        }
        return sessions.fromToken(LoginType.TOKEN, token, credentials.client());
    }
}
