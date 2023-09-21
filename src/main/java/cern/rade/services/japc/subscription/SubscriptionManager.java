package cern.rade.services.japc.subscription;


import cern.rade.services.japc.ContextProvider;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.CloseReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.*;
@Component
public class SubscriptionManager {
    private final long pingToClientFrequency = ContextProvider.getServiceProperties().getPingToClientFrequency();
    private ConcurrentHashMap<Integer, SubscriptionWebsocket> sockets;
    private ConcurrentHashMap<Integer, SubscriptionHandler> clients;
    private ScheduledExecutorService pingSchedulerService;
    private ExecutorService pingService;
    private final Logger logger = LoggerFactory.getLogger(SubscriptionManager.class);

    public SubscriptionManager(){
    }

    @PostConstruct
    public void init(){
        pingSchedulerService = Executors.newSingleThreadScheduledExecutor();
        pingService = Executors.newCachedThreadPool();
        sockets = new ConcurrentHashMap<>();
        clients = new ConcurrentHashMap<>();
        pingSchedulerService.scheduleAtFixedRate(this::pingSockets,0, pingToClientFrequency, TimeUnit.SECONDS);
    }

    public void pingSocket(SubscriptionWebsocket socket){
        try {
            socket.ping();
        } catch (IOException e) {
            logger.error("Error pinging client", e);
            closeUnresponsiveClient(socket.getId());
        }
    }



    public void pingSockets(){
        sockets.forEach((id, socket)-> submitToPingService(socket));
    }

    public void submitToPingService(SubscriptionWebsocket socket){
        pingService.submit(() -> pingSocket(socket));
    }
    public void addSubSock(int sessionID, SubscriptionWebsocket sub){
        sockets.put(sessionID, sub);
    }
    public void removeSubSock(int id){
        sockets.remove(id);
    }
    public SubscriptionWebsocket getSubSock(int id){
        return sockets.get(id);
    }
    public void addSubHandler(int id, SubscriptionHandler client){
        clients.put(id, client);
    }
    public SubscriptionHandler removeSubHandler(int id){
        return clients.remove(id);
    }
    public void clearSubHandlers(){
        clients.forEach((id, client) -> client.stopSubscription());
        clients.clear();
    }
    public void clearSubWebsockets(){
        sockets.forEach((id, socket) -> socket.close(new CloseReason(CloseReason.CloseCodes.SERVICE_RESTART, "Cleanup from subscription manager")));
        sockets.clear();
    }

    public void closeTimedOutSubSocket(int id){
        sockets.get(id).close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Slow client, did not respond to pings within 10 seconds"));
        removeSubSock(id);
    }

    public void closeUnresponsiveClient(int id){
        sockets.get(id).close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Max ping retry attempts reached."));
        removeSubSock(id);
    }
    public void shutdownService(){
        clearSubHandlers();
        clearSubWebsockets();
        pingService.shutdown();
        pingSchedulerService.shutdown();
    }

    public void restartService(){
        shutdownService();
        init();
    }

}
