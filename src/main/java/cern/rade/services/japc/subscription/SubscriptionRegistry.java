package cern.rade.services.japc.subscription;

import java.util.List;

/**
 * Read and close access to open subscriptions, without exposing the WebSocket handling.
 */
public interface SubscriptionRegistry {
    List<SubscriptionInfo> subscriptions();

    /**
     * @return false if no subscription has this id
     */
    boolean closeSubscription(String id);
}
