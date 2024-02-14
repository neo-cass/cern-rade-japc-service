package cern.rade.services.japc.subscription;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.javatolv.LVDataEncoder;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.japc.ContextProvider;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.*;

//, configurator = SpringConfigurator.class
@ServerEndpoint(value = "/labview/subscription", configurator = SubscriptionInitialiser.class)
public class SubscriptionWebsocket {
    private String id;
    private RemoteEndpoint.Async asyncRemote;
    private SubscriptionService subService;
    private Session session;
    private final SubscriptionManager subManager = ContextProvider.getSubManager();
    private  Future<Void> future;
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionWebsocket.class);
    private final ExecutorService sendService = Executors.newSingleThreadExecutor();
    private final long socketIdleTimeout = ContextProvider.getServiceProperties().getSocketIdleTimeout();
    private final long waitTimeForNewValueFromSub = ContextProvider.getServiceProperties().getWaitTimeForNewValueFromSub();
    private DataContainer currentValue;
    public SubscriptionWebsocket(){}
    @OnOpen
    public void onOpen(Session session)  {
        session.setMaxIdleTimeout(socketIdleTimeout*1000);
        Thread.currentThread().setName("Subscription-Thread-ID-" + session.getId());
        subManager.addSubSock(session.getId(), this);
        this.session=session;
        this.id = session.getId();
        this.currentValue = subService.getLatest();
        this.asyncRemote=session.getAsyncRemote();
        init();
    }

    private void init() {
        sendService.execute(this::sendData);
    }

    public void sendData() {
        while(session.isOpen() && !Thread.interrupted()) {
            sendLVData();
        }
        future.cancel(true);
    }

    private void sendLVData()  {
        ByteBuffer sendBuffer;
        try {
            boolean newValue = subService.pushLatestIntoContainer(waitTimeForNewValueFromSub);

            if(!newValue){
                return;
            }
            sendBuffer = ByteBuffer.wrap(LVDataEncoder.encode(currentValue));
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
    public String getId() {
        return id;
    }
    public void setSubService(SubscriptionService subService) {
        this.subService = subService;
    }
}
