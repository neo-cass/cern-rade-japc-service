package cern.rade.services.japc.rbac;

import org.springframework.http.HttpHeaders;

public final class RBACHeaders {
    public static final String HEADER = "Rbac";

    private RBACHeaders() {
    }

    public static void setCached(HttpHeaders headers, RBACClient rbacClient) {
        if (rbacClient != null) {
            headers.set(HEADER, "Cached " + rbacClient.getRbac_id());
        }
    }
}
