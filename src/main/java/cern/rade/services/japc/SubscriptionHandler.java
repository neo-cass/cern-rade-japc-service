package cern.rade.services.japc;

import cern.rade.services.japc.messages.Request;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class SubscriptionHandler {
    private static ConcurrentLinkedQueue<SubscriptionWebsocket> subscriptions = new ConcurrentLinkedQueue<>();
    private static ConcurrentHashMap<String, Request> requests = new ConcurrentHashMap<>();
    public static void addSub(SubscriptionWebsocket sub){
        subscriptions.add(sub);
    }

    public static void removeSub(SubscriptionWebsocket sub){
        subscriptions.remove(sub);
    }

    public static void addRequest(Request request){
        requests.put(request.id, request);
    }

    public static Request getRequest(String id){
        System.out.println(requests.get(id).toString());
        return requests.remove(id);
    }
}
