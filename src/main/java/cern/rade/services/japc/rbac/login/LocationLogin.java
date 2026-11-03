package cern.rade.services.japc.rbac.login;

import cern.rade.services.japc.rbac.LoginType;
import cern.rade.services.japc.rbac.RbacSession;
import cern.rade.services.japc.rbac.RbacSessionFactory;
import cern.rade.services.japc.rbac.gateway.RbacGateway;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;

/**
 * {@code Rbac: Location <base64url(token)>}, a token the client obtained on its own machine. It is used as-is until
 * it expires, because RBAC only renews a token from the address it was issued to.
 */
public class LocationLogin implements RbacLogin {
    private final RbacGateway gateway;
    private final RbacSessionFactory sessions;

    public LocationLogin(RbacGateway gateway, RbacSessionFactory sessions) {
        this.gateway = gateway;
        this.sessions = sessions;
    }

    @Override
    public String scheme() {
        return "Location";
    }

    @Override
    public RbacSession login(RbacCredentials credentials) {
        RbaToken token = gateway.validate(credentials.decodedValue());
        if (!token.isValid()) {
            throw new AuthenticationException("RBAC token has expired. Please log in again");
        }
        return sessions.fromToken(LoginType.LOCATION, token);
    }
}
