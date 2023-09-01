package cern.rade.services.japc.subscription;
import cern.rade.services.codec.CodecException;
import cern.rade.services.japc.Client;
import cern.rade.services.japc.ErrorUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

@Component
@ServerEndpoint(value = "/subs/{id}")
public class SubscriptionWebsocket {
    private int id;
    private RemoteEndpoint.Basic remote;
    private RemoteEndpoint.Async asyncRemote;
    private SubscriptionHandler handler;
    private Session session;
    private Client client;
    private ByteBuffer sendBuffer;
    private Future<Void> future;
    private AtomicReference<Long> lastPongReceived = new AtomicReference<>(System.currentTimeMillis());
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionWebsocket.class);
    @OnOpen
    public void onOpen(Session session, @PathParam("id") int id)  {
        session.setMaxIdleTimeout(0);
        Thread.currentThread().setName("Subscription-Thread-ID-" + id);
        this.id=id;
        this.handler = SubscriptionManager.getSubClient(id);
        SubscriptionManager.addSubSock(id, this);
        this.session=session;
        this.client = handler.getClient();
        this.remote=session.getBasicRemote();
        this.asyncRemote=session.getAsyncRemote();
        init(session);
    }

    private void init(Session session) {
        if(handler.isMonitoring()){
            while(true){sendData();}
        }
    }

    public void sendData() {
        switch(client.getRequestLanguage()){
            case LABVIEW -> sendLVData();
            case PYTHON -> sendPYData();
        }
    }

    private void sendLVData()  {
        try {
            sendBuffer = ByteBuffer.wrap(handler.getLVData(30));
            future = asyncRemote.sendBinary(sendBuffer);
            future.get();
        } catch (CodecException | InterruptedException | ExecutionException e) {
            onError(session, e);
        }
    }

    private void sendPYData() {

    }

    private void sendText(String value)  {
        try {
            remote.sendText(value);
        } catch (IOException e) {
            logger.error("Exception sending text", e);
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) throws ExecutionException, InterruptedException {
        if (message.equals("STOP_SUBSCRIPTION")){
            future.get();
            asyncRemote.sendText("DONE").get();
            close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Deliberate disconnect"));
        }
    }

    @OnClose
    public void onClose()  {
        future.cancel(true);
        close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Deliberate disconnect"));
    }

    @OnError
    public void onError(Session session, Throwable throwable)  {
        String message = "Exception occurred in websocket.";
        logger.error(message, throwable);
        future.cancel(true);
        //TODO error can be sent in the close message
        sendText("ERROR");
        sendText(ErrorUtil.createErrorMessage((Exception) throwable, message));
        close(new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, throwable.getMessage()));
    }

    @OnMessage
    public void onPongMessage(Session session, PongMessage message){
        lastPongReceived.set(System.currentTimeMillis());
    }

    public void close(CloseReason reason){
        SubscriptionManager.removeSubSock(id);
        SubscriptionManager.removeSubClient(id);
        logger.debug("Removing subscription socket and client object for client");
        try {
            session.close(reason);
        } catch (IOException e) {
            logger.error("Error while ending session", e);
        }
    }
}
