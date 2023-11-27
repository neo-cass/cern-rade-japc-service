package cern.rade.services.japc.subscription;
import cern.rade.services.codec.CodecException;
import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.Client;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.server.standard.SpringConfigurator;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
//, configurator = SpringConfigurator.class
@ServerEndpoint(value = "/subscription", configurator = SubscriptionEndpointConfig.class)
public class SubscriptionWebsocket {
    private int id;
    private RemoteEndpoint.Async asyncRemote;
    private SubscriptionHandler handler;
    private Session session;
    private Client client;
    private final SubscriptionManager subManager = ContextProvider.getSubManager();
    private  Future<Void> future;
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionWebsocket.class);
    private final ExecutorService sendService = Executors.newSingleThreadExecutor();
    private final long socketIdleTimeout = ContextProvider.getServiceProperties().getSocketIdleTimeout();
    private final long waitTimeForNewValueFromSub = ContextProvider.getServiceProperties().getWaitTimeForNewValueFromSub();
    public SubscriptionWebsocket(){}
    @OnOpen
    public void onOpen(Session session)  {
        session.setMaxIdleTimeout(socketIdleTimeout*1000);
        Thread.currentThread().setName("Subscription-Thread-ID-" + session.getId());
        subManager.addSubSock(handler.getSub_id(), this);
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
        byte[] data;
        ByteBuffer sendBuffer;
        try {
            data = handler.getLVData(waitTimeForNewValueFromSub);
            if(data==null){
                return;
            }
            sendBuffer = ByteBuffer.wrap(data);
        } catch (InterruptedException e) {
            closeOnError("Handler interrupted while waiting for new value.", e);
            return;
        } catch (CodecException e) {
            closeOnError("Received data did not match type descriptor provided.", e);
            return;
        }

        try {
            future = asyncRemote.sendBinary(sendBuffer);
            future.get();
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

    public void close(CloseReason reason){
        subManager.removeSubSock(id);
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
        close(new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, e.getMessage()));
    }
    public int getId() {
        return id;
    }
    public void setSubHandler(SubscriptionHandler handler) {
        this.handler = handler;
    }
}
