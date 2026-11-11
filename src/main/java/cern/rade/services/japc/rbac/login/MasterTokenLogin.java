package cern.rade.services.japc.rbac.login;

import cern.rade.services.japc.rbac.LoginType;
import cern.rade.services.japc.rbac.RbacProperties;
import cern.rade.services.japc.rbac.RbacSession;
import cern.rade.services.japc.rbac.RbacSessionFactory;
import cern.rade.services.japc.rbac.gateway.RbacGateway;
import cern.rbac.common.MasterToken;

import java.time.Duration;

/**
 * A scheme where this service logs in to RBAC itself, so it holds a master token and can renew the session.
 */
public abstract class MasterTokenLogin implements RbacLogin {
    protected final RbacGateway gateway;
    private final String scheme;
    private final LoginType loginType;
    private final RbacSessionFactory sessions;
    private final RbacProperties properties;

    protected MasterTokenLogin(String scheme, LoginType loginType, RbacGateway gateway, RbacSessionFactory sessions,
                               RbacProperties properties) {
        this.scheme = scheme;
        this.loginType = loginType;
        this.gateway = gateway;
        this.sessions = sessions;
        this.properties = properties;
    }

    @Override
    public String scheme() {
        return scheme;
    }

    @Override
    public final RbacSession login(RbacCredentials credentials) {
        return sessions.fromMaster(loginType, obtainMaster(credentials, properties.sessionLifetime()), credentials.client());
    }

    protected abstract MasterToken obtainMaster(RbacCredentials credentials, Duration lifetime);
}
