package cern.rade.services.japc.rbac.login;

import cern.rade.services.japc.rbac.RbacSession;

/**
 * One scheme of the {@code Rbac: <scheme> <value>} header.
 */
public interface RbacLogin {
    String scheme();

    /**
     * True if the scheme logs in with credentials, which clients may only do at {@code PUT /rbac/session}, and
     * whose session is cached. False for {@code Cached}, which presents an existing session, and {@code Token}, which
     * is validated without a session; both are accepted on every request.
     */
    default boolean performsLogin() {
        return true;
    }

    /**
     * @throws cern.rbac.client.authentication.AuthenticationException if the credentials are rejected
     */
    RbacSession login(RbacCredentials credentials);
}
