package cern.rade.services.japc.rbac;

import org.springframework.http.HttpHeaders;

/**
 * The {@code Rbac} header and its {@code Cached} scheme.
 */
public final class RbacHeaders {
    public static final String HEADER = "Rbac";
    public static final String CACHED = "Cached";

    private RbacHeaders() {
    }

    /**
     * Tells the client how to reuse its session: {@code Rbac: Cached <id>}, and marks the session issued so it is kept.
     */
    public static void setCached(HttpHeaders headers, RbacSession session) {
        if (session != null) {
            headers.set(HEADER, CACHED + " " + session.id());
            session.markIssued();
        }
    }
}
