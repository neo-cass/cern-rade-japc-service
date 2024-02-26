package cern.rade.services.japc.subscription;

import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.javatolv.LVDataEncoder;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.japc.ContextProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LVSubscriptionHandler implements SubscriptionHandler {
    private final Logger logger = LoggerFactory.getLogger(SubscriptionHandshakeInterceptor.class);
    private final long waitTimeForNewValueFromSub = ContextProvider.getServiceProperties().getWaitTimeForNewValueFromSub();
    private ConcurrentWebSocketSessionDecorator session;
    private String ID;
    private SubscriptionService subscriptionService;
    private final ExecutorService sendService = Executors.newSingleThreadExecutor();

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        this.session = (ConcurrentWebSocketSessionDecorator) session;
        this.ID = session.getId();
        this.subscriptionService = (SubscriptionService) this.session.getAttributes().get("subscriptionService");
        streamData();
    }

    private void streamData() {
        sendService.submit(this::send);
    }

    private void send() {
        try {
            DataContainer data = subscriptionService.getLatest();
            byte[] bytes = LVDataEncoder.encode(data);
            session.sendMessage(new BinaryMessage(bytes));
            subscriptionService.pushLatestIntoContainer(waitTimeForNewValueFromSub);
        } catch (IOException e) {
            logger.error("Error sending data to subscription client with ID {}", ID, e);
        } catch (CodecException e) {
            logger.error("Error encoding data for subscription client with ID {}", ID, e);
        }
    }

    @Override
    public void handleMessage(@NotNull WebSocketSession session, @NotNull WebSocketMessage<?> message) throws Exception {

    }

    @Override
    public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) throws Exception {
        this.session.close();
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus closeStatus) throws Exception {
        logger.info("Subscription with ID {} has been closed with reason {}", ID, closeStatus.getReason());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void ping() throws IOException {
        PingMessage pingMessage = new PingMessage();
        session.sendMessage(pingMessage);
    }

    @Override
    public void closeSession(CloseStatus closeStatus) throws IOException {
        session.close(closeStatus);
    }

    @Override
    public String getId() {
        return ID;
    }

}
