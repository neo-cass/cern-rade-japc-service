package cern.rade.services.japc.rbac.login;

import cern.rade.services.japc.rbac.RbacSession;

/**
 * One scheme of the {@code Rbac: <scheme> <value>} header.
 */
public interface RbacLogin {
    String scheme();

    /**
     * @throws cern.rbac.client.authentication.AuthenticationException if the credentials are rejected
     */
    RbacSession login(RbacCredentials credentials);
}
