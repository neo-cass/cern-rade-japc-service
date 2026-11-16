package cern.rade.services.japc.subscription;

/**
 * Keys of the WebSocket session attributes that the handshake hands over to the subscription handler.
 */
public final class SessionAttributes {
    public static final String SUBSCRIPTION_SERVICE = "subscriptionService";
    public static final String FORMAT = "format";
    public static final String FIELDS = "fields";
    public static final String CLIENT = "client";
    public static final String OPENED_AT = "openedAt";

    private SessionAttributes() {
    }
}
