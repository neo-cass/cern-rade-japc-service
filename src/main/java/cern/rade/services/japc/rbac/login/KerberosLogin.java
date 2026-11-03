package cern.rade.services.japc.rbac.login;

import cern.rade.services.japc.rbac.LoginType;
import cern.rade.services.japc.rbac.RbacProperties;
import cern.rade.services.japc.rbac.RbacSessionFactory;
import cern.rade.services.japc.rbac.gateway.RbacGateway;
import cern.rbac.common.MasterToken;

import java.time.Duration;

/**
 * {@code Rbac: Kerberos <base64url(service ticket)>}. The ticket must be for the RBAC server
 * ({@code RBAC/rbac-pro-lb.cern.ch@CERN.CH} in PRO), not for this service.
 */
public class KerberosLogin extends MasterTokenLogin {

    public KerberosLogin(RbacGateway gateway, RbacSessionFactory sessions, RbacProperties properties) {
        super("Kerberos", LoginType.KERBEROS, gateway, sessions, properties);
    }

    @Override
    protected MasterToken obtainMaster(RbacCredentials credentials, Duration lifetime) {
        return gateway.kerberosMaster(credentials.decodedValue(), lifetime);
    }
}
