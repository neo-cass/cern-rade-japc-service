package cern.rade.services.japc.subscription;

import jakarta.websocket.CloseReason;

import java.util.concurrent.*;

public final class SubscriptionManager {
    private static final ConcurrentHashMap<Integer, SubscriptionWebsocket> sockets = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, SubscriptionHandler> clients = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService pingService = Executors.newSingleThreadScheduledExecutor();
    public static void init(){
        pingService.scheduleAtFixedRate(new SubscriptionPinger(sockets), 0, 10, TimeUnit.SECONDS);
        ScheduledExecutorService s = Executors.newScheduledThreadPool(10);
    }
    public static void addSubSock(int sessionID, SubscriptionWebsocket sub){
        sockets.put(sessionID, sub);
    }
    public static void removeSubSock(int id){
        sockets.remove(id);
    }
    public static SubscriptionWebsocket getSubSock(int id){
        return sockets.get(id);
    }
    public static void addSubHandler(int id, SubscriptionHandler client){
        clients.put(id, client);
    }
    public static void removeSubHandler(int sessionID){
        clients.remove(sessionID);
    }
    public static SubscriptionHandler getSubHandler(int id){
        return clients.get(id);
    }
    public static void clearSubHandlers(){
        clients.forEach((id, client) -> client.stopSubscription());
        clients.clear();
    }
    public static void clearSubWebsockets(){
        sockets.forEach((id, socket) -> socket.close(new CloseReason(CloseReason.CloseCodes.SERVICE_RESTART, "Cleanup from subscription manager")));
        sockets.clear();
    }

    public static void closeSlowSubSocket(int id){
        sockets.get(id).close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Not responding to ping"));
        removeSubSock(id);
    }


}
