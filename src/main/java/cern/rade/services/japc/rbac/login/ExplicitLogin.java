package cern.rade.services.japc.rbac.login;

import cern.rade.services.japc.rbac.LoginType;
import cern.rade.services.japc.rbac.RbacProperties;
import cern.rade.services.japc.rbac.RbacSessionFactory;
import cern.rade.services.japc.rbac.gateway.RbacGateway;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.MasterToken;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * {@code Rbac: Explicit <base64url(user:password)>}
 */
public class ExplicitLogin extends MasterTokenLogin {

    public ExplicitLogin(RbacGateway gateway, RbacSessionFactory sessions, RbacProperties properties) {
        super("Explicit", LoginType.EXPLICIT, gateway, sessions, properties);
    }

    @Override
    protected MasterToken obtainMaster(RbacCredentials credentials, Duration lifetime) {
        String userAndPassword = new String(credentials.decodedValue(), StandardCharsets.ISO_8859_1);
        int separator = userAndPassword.indexOf(':');
        if (separator <= 0) {
            throw new AuthenticationException("Invalid credentials format for Rbac Explicit. Expected base64url(user:password)");
        }
        String user = userAndPassword.substring(0, separator);
        char[] password = userAndPassword.substring(separator + 1).toCharArray();
        return gateway.explicitMaster(user, password, lifetime);
    }
}
