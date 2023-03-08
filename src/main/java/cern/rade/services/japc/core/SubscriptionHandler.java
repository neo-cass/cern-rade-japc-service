package cern.rade.services.japc.core;

import cern.rade.services.japc.communication.SubscriptionWebsocket;

import java.util.concurrent.ConcurrentLinkedQueue;

public final class SubscriptionHandler {
    private static ConcurrentLinkedQueue<SubscriptionWebsocket> subscriptions;
    public static void addSub(SubscriptionWebsocket sub){
        if(subscriptions==null){subscriptions = new ConcurrentLinkedQueue<>();}
        subscriptions.add(sub);
    }

    public static void removeSub(SubscriptionWebsocket sub){
        if(subscriptions==null){subscriptions = new ConcurrentLinkedQueue<>();}
        subscriptions.remove(sub);
    }
}
