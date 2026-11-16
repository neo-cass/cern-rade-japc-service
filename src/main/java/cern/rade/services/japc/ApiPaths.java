package cern.rade.services.japc;

/**
 * Route prefixes and path patterns of the API, shared by the controllers and the security configuration.
 * <p>
 * Every API version has its own prefix ({@code /api/v1}, {@code /ws/v1}). The unversioned prefixes {@code /api} and
 * {@code /ws} always serve the latest version, today v1. A path segment {@code v<number>} directly after a prefix is
 * a version, never a device. When a v2 is added, its controllers list {@code {API, API_V2}} and the v1 controllers
 * keep only {@code API_V1}.
 */
public final class ApiPaths {
    public static final String API = "/api";
    public static final String API_V1 = "/api/v1";
    public static final String WS = "/ws";
    public static final String WS_V1 = "/ws/v1";
    public static final String ADMIN = "/admin";
    public static final String ADMIN_API = ADMIN + "/api";
    public static final String RBAC = "/rbac";
    public static final String RBAC_SESSION = RBAC + "/session";
    public static final String DEVICE = "/{device:(?!v[0-9]+$).+}";
    public static final String DEVICE_PROPERTY = DEVICE + "/{property}";
    public static final String DEVICE_PROPERTY_FIELD = DEVICE_PROPERTY + "/{field}";

    private ApiPaths() {
    }

    public static String stripWebSocketBase(String path) {
        String rest;
        if (path.startsWith(WS_V1 + "/")) {
            rest = path.substring(WS_V1.length());
        } else if (path.startsWith(WS + "/")) {
            rest = path.substring(WS.length());
        } else {
            return null;
        }
        // same rule as DEVICE for REST: a version segment is not a device
        return rest.matches("/v[0-9]+(/.*)?") ? null : rest;
    }
}
