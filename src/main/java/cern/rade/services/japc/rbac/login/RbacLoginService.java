package cern.rade.services.japc.rbac.login;

import cern.rade.services.japc.rbac.RbacSession;
import cern.rade.services.japc.rbac.RbacSessionRegistry;
import cern.rbac.client.authentication.AuthenticationException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Logs in with whichever {@link RbacLogin} handles the scheme of the {@code Rbac} header, and caches the session.
 */
public class RbacLoginService {
    private final Map<String, RbacLogin> logins;
    private final RbacSessionRegistry registry;

    public RbacLoginService(List<RbacLogin> logins, RbacSessionRegistry registry) {
        this.logins = logins.stream().collect(Collectors.toUnmodifiableMap(RbacLogin::scheme, Function.identity()));
        this.registry = registry;
    }

    /**
     * @return true if the scheme exists and logs in with credentials rather than presenting a cached session or token
     * @see RbacLogin#performsLogin()
     */
    public boolean performsLogin(String scheme) {
        RbacLogin login = logins.get(scheme);
        return login != null && login.performsLogin();
    }

    public RbacSession login(String header) {
        return login(RbacCredentials.parse(header));
    }

    public RbacSession login(RbacCredentials credentials) {
        RbacLogin login = logins.get(credentials.scheme());
        if (login == null) {
            throw new AuthenticationException("Invalid authentication type " + credentials.scheme()
                    + ". Expected one of " + logins.keySet());
        }
        RbacSession session = login.login(credentials);
        return session.loginType().cached() ? registry.register(session) : session;
    }
}
