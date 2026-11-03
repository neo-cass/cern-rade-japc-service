package cern.rade.services.japc.admin.view;

import cern.rade.services.japc.rbac.LoginType;
import cern.rade.services.japc.rbac.RbacSession;

import java.util.Set;

/**
 * The logged-in admin.
 */
public record MeView(String user, LoginType loginType, Set<String> roles) {

    public static MeView of(RbacSession session) {
        return new MeView(session.user(), session.loginType(), session.roles());
    }
}
