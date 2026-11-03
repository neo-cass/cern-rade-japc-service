package cern.rade.services.japc.subscription;

import java.time.Instant;

/**
 * An open subscription, as shown in the admin panel.
 */
public record SubscriptionInfo(String id, String parameter, String selector, String format, String user,
                               String address, Instant openedAt) {
}
