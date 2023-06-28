package cern.rade.services.japc.subscription;

import jakarta.websocket.CloseReason;

import java.util.concurrent.ConcurrentHashMap;

public final class SubscriptionManager {
    private static ConcurrentHashMap<Integer, SubscriptionWebsocket> sockets = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer, SubscriptionHandler> clients = new ConcurrentHashMap<>();
    public static void addSubSock(int sessionID, SubscriptionWebsocket sub){
        sockets.put(sessionID, sub);
    }
    public static void removeSubSock(int id){
        sockets.remove(id);
    }
    public static SubscriptionWebsocket getSubSock(int id){
        return sockets.get(id);
    }
    public static void addSubClient(int id, SubscriptionHandler client){
        clients.put(id, client);
    }
    public static void removeSubClient(int sessionID){
        clients.remove(sessionID);
    }
    public static SubscriptionHandler getSubClient(int id){
        return clients.get(id);
    }
    public static void clearSubClients(){
        clients.forEach((id, client) -> client.stopSubscription());
        clients.clear();
    }
    public static void clearSubWebsockets(){
        sockets.forEach((id, socket) -> socket.close(new CloseReason(CloseReason.CloseCodes.SERVICE_RESTART, "Cleanup from subscription manager")));
        sockets.clear();
    }
}
