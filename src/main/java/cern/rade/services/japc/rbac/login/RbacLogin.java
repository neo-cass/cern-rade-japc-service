package cern.rade.services.japc.rbac.login;

import cern.rade.services.japc.rbac.RbacSession;

/**
 * One scheme of the {@code Rbac: <scheme> <value>} header.
 */
public interface RbacLogin {
    String scheme();

    /**
     * True if the scheme logs in, with credentials or an RBAC token, which clients may only do at
     * {@code PUT /rbac/session}. False for {@code Cached}, which presents an existing session and is accepted on every
     * request.
     */
    default boolean performsLogin() {
        return true;
    }

    /**
     * @throws cern.rbac.client.authentication.AuthenticationException if the credentials are rejected
     */
    RbacSession login(RbacCredentials credentials);
}
