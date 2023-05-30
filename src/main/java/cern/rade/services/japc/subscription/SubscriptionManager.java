package cern.rade.services.japc.subscription;

import cern.rade.services.common.communication.Request;

import java.util.concurrent.ConcurrentHashMap;

public final class SubscriptionManager {
    private static ConcurrentHashMap<Integer, SubscriptionWebsocket> sockets = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer, SubscriptionClient> clients = new ConcurrentHashMap<>();
    public static void addSubSock(int sessionID, SubscriptionWebsocket sub){
        sockets.put(sessionID, sub);
    }
    public static void removeSubSock(int id){
        sockets.remove(id);
    }
    public static SubscriptionWebsocket getSubSock(int id){
        return sockets.get(id);
    }
    public static void addSubClient(int id, SubscriptionClient client){
        clients.put(id, client);
    }
    public static void removeSubClient(int sessionID){
        clients.remove(sessionID);
    }
    public static SubscriptionClient getSubClient(int id){
        return clients.get(id);
    }
}
