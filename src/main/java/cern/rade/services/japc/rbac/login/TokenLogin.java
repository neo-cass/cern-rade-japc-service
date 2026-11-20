package cern.rade.services.japc.rbac.login;

import cern.rade.services.japc.rbac.LoginType;
import cern.rade.services.japc.rbac.RbacSession;
import cern.rade.services.japc.rbac.RbacSessionFactory;
import cern.rade.services.japc.rbac.TokenValidationCache;
import cern.rbac.common.RbaToken;

/**
 * {@code Rbac: Token <base64url(token)>}, an RBAC token the client obtained on its own machine. It is stateless: the
 * client sends it on every request and no session is cached for it, only the result of validating its signature (see
 * {@link TokenValidationCache}). It is never renewed, because RBAC only renews a token from the address it was issued
 * to: requests are rejected once the token expires.
 */
public class TokenLogin implements RbacLogin {
    public static final String SCHEME = "Token";
    private final TokenValidationCache tokens;
    private final RbacSessionFactory sessions;

    public TokenLogin(TokenValidationCache tokens, RbacSessionFactory sessions) {
        this.tokens = tokens;
        this.sessions = sessions;
    }

    @Override
    public String scheme() {
        return SCHEME;
    }

    /**
     * False: the token is accepted on every request, and the session built from it is not registered.
     */
    @Override
    public boolean performsLogin() {
        return false;
    }

    @Override
    public RbacSession login(RbacCredentials credentials) {
        RbaToken token = tokens.validate(credentials.decodedValue());
        return sessions.fromToken(LoginType.TOKEN, token, credentials.client());
    }
}
