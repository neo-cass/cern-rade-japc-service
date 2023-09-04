package cern.rade.services.japc.subscription;
import cern.rade.services.codec.CodecException;
import cern.rade.services.japc.Client;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.SpringConfigurator;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

@Component
@ServerEndpoint(value = "/subs/{id}", configurator = SpringConfigurator.class)
public class SubscriptionWebsocket {
    private int id;
    private RemoteEndpoint.Basic remote;
    private RemoteEndpoint.Async asyncRemote;
    private SubscriptionHandler handler;
    private Session session;
    private Client client;
    private final AtomicReference<Long> lastPongReceived = new AtomicReference<>(System.currentTimeMillis());
    private final AtomicReference<Integer> retry = new AtomicReference<>(3);
    private final AtomicReference<Boolean> pingOngoing = new AtomicReference<>(false);
    private  Future<Void> future;
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionWebsocket.class);
    public SubscriptionWebsocket(){}
    @OnOpen
    public void onOpen(Session session, @PathParam("id") int id)  {
        session.setMaxIdleTimeout(10000);
        Thread.currentThread().setName("Subscription-Thread-ID-" + id);
        this.id=id;
        this.handler = SubscriptionManager.getSubHandler(id);
        SubscriptionManager.addSubSock(id, this);
        this.session=session;
        this.client = handler.getClient();
        this.remote=session.getBasicRemote();
        this.asyncRemote=session.getAsyncRemote();
        init();
    }

    private void init() {
        if(handler.isMonitoring()){
            sendData();
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
            ByteBuffer sendBuffer = ByteBuffer.wrap(handler.getLVData(1000));
            future = asyncRemote.sendBinary(sendBuffer);
            future.get();
        } catch (CodecException e) {

        } catch(CancellationException e){

        } catch(ExecutionException e){

        } catch (InterruptedException e){

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
    public void onMessage(Session session, String message) {

    }

    @OnClose
    public void onClose(CloseReason closeReason) {
        future.cancel(true);
        logger.info("Closing subscription: " + id + " with reason " + closeReason.getReasonPhrase());
    }

    public void ping(){
        try {
            asyncRemote.sendPing(ByteBuffer.wrap(new byte[0]));
        } catch(CancellationException e){

        } catch (IOException e) {

        }
    }

    @OnError
    public void onError(Throwable throwable)  {
        String message = "Exception occurred in websocket.";
        logger.error("Exception in subscription client: " + id + message, throwable);
        future.cancel(true);
        //TODO error can be sent in the close message
//        sendText("ERROR");
//        sendText(ErrorUtil.createErrorMessage((Exception) throwable, message));
        close(new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, throwable.getMessage()));
    }

    @OnMessage
    public void onPongMessage(Session session, PongMessage message){
        lastPongReceived.set(System.currentTimeMillis());
    }

    public void close(CloseReason reason){
        SubscriptionManager.removeSubSock(id);
        logger.debug("Removing subscription socket and client object for client");
        try {
            session.close(reason);
        } catch (IOException e) {
            logger.error("Error while ending session", e);
        }
    }

    public boolean hasTimedOut(){
        long diff = System.currentTimeMillis() - lastPongReceived.get();
        return diff > 10000;
    }

    public void decrementRetry() {
        retry.getAndUpdate((val) -> val-1);
    }

    public void resetRetry(){
        retry.compareAndSet(0, 3);
    }
}
