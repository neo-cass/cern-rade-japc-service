package cern.rade.services.japc.stats;

import cern.rade.services.japc.rbac.RbacSession;
import cern.rade.services.japc.security.RbacAuthentication;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Who made a request: the RBAC user and the client's address (the real one behind the router, see
 * {@code server.forward-headers-strategy}).
 */
public record Client(String user, String address) {
    public static final String ANONYMOUS = "anonymous";

    public static Client of(HttpServletRequest request) {
        String user = RbacAuthentication.current().map(RbacSession::user).orElse(ANONYMOUS);
        return new Client(user, request.getRemoteAddr());
    }
}
