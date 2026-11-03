package cern.rade.services.japc.rbac.login;

import cern.rade.services.japc.rbac.RbacHeaders;
import cern.rade.services.japc.rbac.RbacSession;
import cern.rade.services.japc.rbac.RbacSessionRegistry;
import cern.rbac.client.authentication.AuthenticationException;

/**
 * {@code Rbac: Cached <id>}, reusing a session from an earlier login.
 */
public class CachedLogin implements RbacLogin {
    private final RbacSessionRegistry registry;

    public CachedLogin(RbacSessionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String scheme() {
        return RbacHeaders.CACHED;
    }

    @Override
    public RbacSession login(RbacCredentials credentials) {
        return registry.find(credentials.value())
                .orElseThrow(() -> new AuthenticationException("No RBAC session for this cached id. Please log in again"));
    }
}
