package cern.rade.services.japc.subscription;

import cern.rade.services.codec.CodecException;
import cern.rade.services.japc.ContextProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class SubscriptionWebSocketHandler implements WebSocketHandler {
    private final Logger logger = LoggerFactory.getLogger(SubscriptionWebSocketHandler.class);
    protected final long waitTimeForNewValueFromSub = ContextProvider.getServiceProperties().getWaitTimeForNewValueFromSub();
    protected ConcurrentWebSocketSessionDecorator session;
    protected String ID;
    protected SubscriptionService subscriptionService;
    protected boolean newValue = false;
    private final ExecutorService sendService = Executors.newSingleThreadExecutor();

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        this.session = (ConcurrentWebSocketSessionDecorator) session;
        this.ID = session.getId();
        this.subscriptionService = (SubscriptionService) this.session.getAttributes().get("subscriptionService");
        streamData();
    }

    private void streamData() {
        sendService.submit(this::iterativelySendData);
    }
    private void iterativelySendData(){
        while(session.isOpen() && !Thread.currentThread().isInterrupted()){
            try {
                newValue = sendData();
            } catch (IOException e) {
                logger.error("Error while sending data to the subscription client with ID {}", ID, e);
            } catch (CodecException e) {
                logger.error("Error while encoding data for the subscription client with ID {}", ID, e);;
            }
        }
    }
    abstract boolean sendData() throws IOException, CodecException;

    @Override
    public void handleMessage(@NotNull WebSocketSession session, @NotNull WebSocketMessage<?> message) throws Exception {

    }
    @Override
    public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) throws Exception {
        this.session.close();
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus closeStatus) throws Exception {
        sendService.shutdown();
        subscriptionService.stopSubscription();
        logger.info("Subscription with ID {} has been closed with reason {}", ID, closeStatus.getReason());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    protected void ping() throws IOException {
        PingMessage pingMessage = new PingMessage();
        session.sendMessage(pingMessage);
    }

    protected void closeSession(CloseStatus closeStatus) throws IOException {
        session.close(closeStatus);
    }

    protected String getId() {
        return ID;
    }

    protected boolean hasNewValue() {
        return newValue;
    }
}
