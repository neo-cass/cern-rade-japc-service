package cern.rade.services.japc;

public final class ApiPaths {
    public static final String API = "/api";
    public static final String API_V1 = "/api/v1";
    public static final String WS = "/ws";
    public static final String WS_V1 = "/ws/v1";
    public static final String DEVICE = "/{device:(?!v[0-9]+$).+}";
    public static final String DEVICE_PROPERTY = DEVICE + "/{property}";
    public static final String DEVICE_PROPERTY_FIELD = DEVICE_PROPERTY + "/{field}";

    private ApiPaths() {
    }

    public static String stripWebSocketBase(String path) {
        if (path.startsWith(WS_V1 + "/")) {
            return path.substring(WS_V1.length());
        }
        if (path.startsWith(WS + "/")) {
            return path.substring(WS.length());
        }
        return null;
    }
}
