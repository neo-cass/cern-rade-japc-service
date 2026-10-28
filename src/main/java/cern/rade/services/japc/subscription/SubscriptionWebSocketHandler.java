package cern.rade.services.japc.subscription;

import cern.japc.core.ParameterException;
import cern.rade.services.codec.CodecException;
import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.Fields;
import cern.rade.services.japc.error.ErrorCode;
import cern.rade.services.japc.format.Format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubscriptionWebSocketHandler implements WebSocketHandler {
    private final Logger logger = LoggerFactory.getLogger(SubscriptionWebSocketHandler.class);
    protected final long waitTimeForNewValueFromSub = ContextProvider.getServiceProperties().getWaitTimeForNewValueFromSub();
    protected ConcurrentWebSocketSessionDecorator session;
    protected String ID;
    protected SubscriptionService subscriptionService;
    protected Format format;
    protected String origin;
    protected List<String> fields;
    private final ExecutorService sendService = Executors.newSingleThreadExecutor();

    @Override
    @SuppressWarnings("unchecked")
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.session = (ConcurrentWebSocketSessionDecorator) session;
        this.ID = session.getId();
        this.subscriptionService = (SubscriptionService) this.session.getAttributes().get(SessionAttributes.SUBSCRIPTION_SERVICE);
        this.format = (Format) this.session.getAttributes().get(SessionAttributes.FORMAT);
        this.origin = "GET " + session.getUri().getPath();
        this.fields = (List<String>) this.session.getAttributes().get(SessionAttributes.FIELDS);
        streamData();
    }

    private void streamData() {
        sendService.submit(this::iterativelySendData);
    }
    private void iterativelySendData(){
        while(session.isOpen() && !Thread.currentThread().isInterrupted()){
            try {
                sendData();
            } catch (ParameterException e) {
                logger.warn("JAPC error in the subscription for client with ID {}", ID, e);
                sendError(e);
            } catch (IOException e) {
                logger.error("Error while sending data to the subscription client with ID {}", ID, e);
                close(CloseStatus.SERVER_ERROR);
            } catch (Exception e) {
                logger.error("Fatal error in the subscription for client with ID {}", ID, e);
                close(ErrorCode.of(e).closeStatus(ErrorCode.message(e)));
            }
        }
    }

    private void sendData() throws IOException, CodecException, ParameterException {
        if (subscriptionService.pushLatestIntoContainer(waitTimeForNewValueFromSub)) {
            session.sendMessage(format.dataFrame(Fields.select(subscriptionService.getLatest(), fields), subscriptionService.getLatestHeader(), session.getAttributes()));
        }
    }

    private void sendError(Exception e) {
        try {
            session.sendMessage(format.errorFrame(ErrorCode.of(e), origin, ErrorCode.message(e)));
        } catch (Exception sendException) {
            logger.error("Could not send error to the subscription client with ID {}", ID, sendException);
            close(ErrorCode.of(e).closeStatus(ErrorCode.message(e)));
        }
    }

    private void close(CloseStatus closeStatus) {
        try {
            closeSession(closeStatus);
        } catch (IOException e) {
            logger.error("Could not close the subscription for client with ID {}", ID, e);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

    }
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        this.session.close();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
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
}
