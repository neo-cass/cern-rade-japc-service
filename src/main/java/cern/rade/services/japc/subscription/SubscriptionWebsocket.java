package cern.rade.services.japc.subscription;
import cern.rade.services.codec.CodecException;
import cern.rade.services.japc.Client;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
// , configurator = SpringConfigurator.class
@Component
@ServerEndpoint(value = "/subs/{id}")
public class SubscriptionWebsocket {
    private int id;
    private RemoteEndpoint.Async asyncRemote;
    private SubscriptionHandler handler;
    private Session session;
    private Client client;
    private final AtomicReference<Long> lastPongReceived = new AtomicReference<>(System.currentTimeMillis());
    private  Future<Void> future;
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionWebsocket.class);
    private final ExecutorService sendService = Executors.newSingleThreadExecutor();
    @Value("${radeservicesjapc.socketIdleTimeout}")
    private Duration socketIdleTimeout;
    @Value("${radeservicesjapc.waitTimeForNewValueFromSub}")
    private Duration waitTimeForNewValueFromSub;
    public SubscriptionWebsocket(){}
    @OnOpen
    public void onOpen(Session session, @PathParam("id") int id)  {
        session.setMaxIdleTimeout(socketIdleTimeout.get(ChronoUnit.MILLIS));
        Thread.currentThread().setName("Subscription-Thread-ID-" + id);
        this.id=id;
        this.handler = SubscriptionManager.removeSubHandler(id);
        SubscriptionManager.addSubSock(id, this);
        this.session=session;
        this.client = handler.getClient();
        this.asyncRemote=session.getAsyncRemote();
        init();
    }

    private void init() {
        sendService.execute(this::sendData);
    }

    public void sendData() {
        while(session.isOpen() && !Thread.interrupted()) {
            switch (client.getRequestLanguage()) {
                case LABVIEW -> sendLVData();
                case PYTHON -> sendPYData();
            }
        }
        future.cancel(true);
    }

    private void sendLVData()  {
        try {
            byte[] data = handler.getLVData(waitTimeForNewValueFromSub.get(ChronoUnit.MILLIS));
            if(data==null){
                return;
            }
            ByteBuffer sendBuffer = ByteBuffer.wrap(data);
            future = asyncRemote.sendBinary(sendBuffer);
            future.get();
        } catch(CodecException e) {
            closeOnError("Received data did not match type descriptor provided.", e);
        } catch(CancellationException e){
            closeOnError("Send operation cancelled in client: " + id, e);
        } catch(ExecutionException e){
            closeOnError("Execution exception in send operation.", e);
            logger.error("Cause of execution exception.", e.getCause());
        } catch (InterruptedException e){
            closeOnError("Sender thread interrupted.", e);
        }
    }

    private void sendPYData() {

    }

    @OnMessage
    public void onMessage(Session session, String message) {

    }

    @OnClose
    public void onClose(CloseReason closeReason) {
        close(closeReason);
        logger.info("Closing subscription: " + id + " with reason " + closeReason.getReasonPhrase());
    }

    public void ping() throws IOException {
        asyncRemote.sendPing(ByteBuffer.wrap(new byte[0]));
    }

    @OnError
    public void onError(Throwable throwable)  {
        closeOnError("Exception in subscription client: " + id, (Exception) throwable);
    }

    @OnMessage
    public void onPongMessage(Session session, PongMessage message){
        lastPongReceived.set(System.currentTimeMillis());
    }

    public void close(CloseReason reason){
        SubscriptionManager.removeSubSock(id);
        sendService.shutdownNow();
        logger.debug("Removing subscription socket and client object for client");
        try {
            if(session.isOpen()){
                session.close(reason);
            }
        } catch (IOException e) {
            logger.error("Error while ending session", e);
        }
    }

    public void closeOnError(String message, Exception e){
        logger.error(message,e);
        close(new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, message));
    }
    public int getId() {
        return id;
    }
}
