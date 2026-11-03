package cern.rade.services.japc.admin.view;

import cern.rade.services.japc.rbac.LoginType;
import cern.rade.services.japc.rbac.RbacSession;

import java.time.Instant;
import java.util.Set;

/**
 * A cached RBAC session without its secret id.
 */
public record SessionView(String handle, String user, LoginType loginType, Set<String> roles, String location,
                          Instant created, Instant lastUsed, Instant tokenExpiry, Instant sessionExpiry) {

    public static SessionView of(RbacSession session) {
        return new SessionView(session.handle(), session.user(), session.loginType(), session.roles(),
                session.location(), session.created(), session.lastUsed(), session.tokenExpiry(), session.sessionExpiry());
    }
}
