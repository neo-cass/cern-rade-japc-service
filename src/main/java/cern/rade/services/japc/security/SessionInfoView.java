package cern.rade.services.japc.security;

import cern.rade.services.japc.rbac.LoginType;
import cern.rade.services.japc.rbac.RbacSession;

import java.time.Instant;
import java.util.Set;

/**
 * The caller's own RBAC session without its secret id.
 */
public record SessionInfoView(String user, Set<String> roles, LoginType loginType, String location,
                              Instant tokenExpiry, Instant sessionExpiry) {

    public static SessionInfoView of(RbacSession session) {
        return new SessionInfoView(session.user(), session.roles(), session.loginType(), session.location(),
                session.tokenExpiry(), session.sessionExpiry());
    }
}
