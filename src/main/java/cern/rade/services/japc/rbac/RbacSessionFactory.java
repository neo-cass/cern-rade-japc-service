package cern.rade.services.japc.rbac;

import cern.rade.services.japc.rbac.gateway.RbacGateway;
import cern.rbac.common.MasterToken;
import cern.rbac.common.RbaToken;

import java.time.Clock;

/**
 * Creates {@link RbacSession}s with the configured token lifetime.
 */
public class RbacSessionFactory {
    private final RbacGateway gateway;
    private final RbacProperties properties;
    private final Clock clock;

    public RbacSessionFactory(RbacGateway gateway, RbacProperties properties, Clock clock) {
        this.gateway = gateway;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * A renewable session for a master token obtained by this service.
     */
    public RbacSession fromMaster(LoginType loginType, MasterToken master) {
        RbacSession.TokenRenewer renewer = token -> gateway.renew(token, properties.tokenLifetime());
        return new RbacSession(loginType, renewer.renew(master), master, renewer, clock);
    }

    /**
     * A session for a token the client obtained itself; it is used until it expires.
     */
    public RbacSession fromToken(LoginType loginType, RbaToken token) {
        return new RbacSession(loginType, token, null, null, clock);
    }
}
