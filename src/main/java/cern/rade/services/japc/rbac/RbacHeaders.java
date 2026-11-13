package cern.rade.services.japc.rbac;

import org.springframework.http.HttpHeaders;

public final class RbacHeaders {
    public static final String HEADER = "Rbac";
    public static final String CACHED = "Cached";

    private RbacHeaders() {
    }

    /**
     * Tells the client how to reuse its session: {@code Rbac: Cached <id>}, and marks the session issued so it is kept.
     * Sessions that are not cached, such as {@code Token} ones, have no id to hand out and get no header.
     */
    public static void setCached(HttpHeaders headers, RbacSession session) {
        if (session != null && session.loginType().cached()) {
            headers.set(HEADER, CACHED + " " + session.id());
            session.markIssued();
        }
    }
}
