package cern.rade.services.japc.subscription;

import jakarta.websocket.CloseReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;
@Component
public final class SubscriptionManager {
    @Value("${radeservicesjapc.pingToClientFrequency}")
    private static Duration pingToClientFrequency;

    private static ConcurrentHashMap<Integer, SubscriptionWebsocket> sockets;
    private static ConcurrentHashMap<Integer, SubscriptionHandler> clients;
    private static ScheduledExecutorService pingSchedulerService;
    private static ExecutorService pingService;
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionManager.class);
    public static void init(){
        pingSchedulerService = Executors.newSingleThreadScheduledExecutor();
        pingService = Executors.newCachedThreadPool();
        sockets = new ConcurrentHashMap<>();
        clients = new ConcurrentHashMap<>();
        logger.info("VALUE: :\n\n"+String.valueOf(pingToClientFrequency));
        pingSchedulerService.scheduleAtFixedRate(SubscriptionManager::pingSockets,0, pingToClientFrequency.get(ChronoUnit.MILLIS), TimeUnit.MILLISECONDS);
    }

    public static void pingSocket(SubscriptionWebsocket socket){
        try {
            socket.ping();
        } catch (IOException e) {
            logger.error("Error pinging client", e);
            closeUnresponsiveClient(socket.getId());
        }
    }

    public static void pingSockets(){
        sockets.forEach((id, socket)-> submitToPingService(socket));
    }

    public static void submitToPingService(SubscriptionWebsocket socket){
        pingService.submit(() -> pingSocket(socket));
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
    public static SubscriptionHandler removeSubHandler(int id){
        return clients.remove(id);
    }
    public static void clearSubHandlers(){
        clients.forEach((id, client) -> client.stopSubscription());
        clients.clear();
    }
    public static void clearSubWebsockets(){
        sockets.forEach((id, socket) -> socket.close(new CloseReason(CloseReason.CloseCodes.SERVICE_RESTART, "Cleanup from subscription manager")));
        sockets.clear();
    }

    public static void closeTimedOutSubSocket(int id){
        sockets.get(id).close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Slow client, did not respond to pings within 10 seconds"));
        removeSubSock(id);
    }

    public static void closeUnresponsiveClient(int id){
        sockets.get(id).close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Max ping retry attempts reached."));
        removeSubSock(id);
    }
    public static void shutdownService(){
        clearSubHandlers();
        clearSubWebsockets();
        pingService.shutdown();
        pingSchedulerService.shutdown();
    }

    public static void restartService(){
        shutdownService();
        init();
    }

}
