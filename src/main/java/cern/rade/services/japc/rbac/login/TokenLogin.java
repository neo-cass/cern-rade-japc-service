package cern.rade.services.japc.rbac.login;

import cern.rade.services.japc.rbac.LoginType;
import cern.rade.services.japc.rbac.RbacSession;
import cern.rade.services.japc.rbac.RbacSessionFactory;
import cern.rade.services.japc.rbac.gateway.RbacGateway;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;

/**
 * {@code Rbac: Token <base64url(token)>}, an RBAC token the client obtained on its own machine. Like the other login
 * schemes it is only accepted by {@code PUT /rbac/session}, which caches it and returns the {@code Cached} id. It is
 * never renewed, because RBAC only renews a token from the address it was issued to: the cached session ends when the
 * token expires.
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
    public RbacSession login(RbacCredentials credentials) {
        RbaToken token = gateway.validate(credentials.decodedValue());
        if (!token.isValid()) {
            throw new AuthenticationException("RBAC token has expired. Please obtain a new one");
        }
        return sessions.fromToken(LoginType.TOKEN, token, credentials.client());
    }
}
