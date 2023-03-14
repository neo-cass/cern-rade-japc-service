package cern.rade.services.japc;

import cern.rade.services.japc.messages.Request;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class SubscriptionHandler {
    private static ConcurrentLinkedQueue<SubscriptionWebsocket> subscriptions;
    private static ConcurrentHashMap<String, Request> requests;
    public static void addSub(SubscriptionWebsocket sub){
        if(subscriptions==null){subscriptions = new ConcurrentLinkedQueue<>();}
        subscriptions.add(sub);
    }

    public static void removeSub(SubscriptionWebsocket sub){
        if(subscriptions==null){subscriptions = new ConcurrentLinkedQueue<>();}
        subscriptions.remove(sub);
    }

    public static void addRequest(Request request){
        if(requests==null){requests = new ConcurrentHashMap<>();}
        requests.put(request.id, request);
    }


    public static Request getRequest(String id){
        if(requests==null){requests = new ConcurrentHashMap<>();}
        return requests.remove(id);
    }
}
