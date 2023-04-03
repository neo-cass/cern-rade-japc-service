package cern.rade.services.japc;

import cern.rade.services.japc.messages.Request;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class SubscriptionHandler {
    private static ConcurrentHashMap<Integer, SubscriptionWebsocket> subscriptions = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer, Request> requests = new ConcurrentHashMap<>();
    public static void addSub(int sessionID, SubscriptionWebsocket sub){
        subscriptions.put(sessionID, sub);
    }
    public static void removeSub(int sessionID){
        subscriptions.remove(sessionID);
    }
    public static void addRequest(Request request){requests.put(request.getId(), request);}
    public static Request getRequest(int id){return requests.get(id);}

    public static Request removeRequest(int id){return requests.remove(id);}
}
