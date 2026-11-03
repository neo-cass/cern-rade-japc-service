package cern.rade.services.japc.security;

import cern.rade.services.japc.rbac.RbacSession;

/**
 * A principal from another login mechanism (e.g. CERN SSO) that carries the RBAC session it was exchanged for.
 */
public interface RbacPrincipal {
    RbacSession rbacSession();
}
