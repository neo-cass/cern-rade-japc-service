package cern.rade.services.japc.subscription;
import cern.japc.core.*;
import cern.rade.services.codec.japcjava.JAPCDecoder;
import cern.rade.services.codec.javatolv.LVTypeEncoder;
import cern.rade.services.common.communication.Request;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.japc.Util;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.SpringConfigurator;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
@ServerEndpoint(value = "/subs/{id}")
public class SubscriptionWebsocket {
    private int id;
    private RemoteEndpoint.Basic remoteEndpoint;
    private RemoteEndpoint.Async async;
    private SubscriptionHandler client;
    private Session session;
    private OutputStreamWriter out;
    private Writer sendWriter;
    private Request request;
    private boolean clientClosed=false;
    private Future<Void> future;
    private Logger logger = LoggerFactory.getLogger(SubscriptionWebsocket.class);
    @OnOpen
    public void onOpen(Session session, @PathParam("id") int id) {
        session.setMaxIdleTimeout(0);
        Thread.currentThread().setName("Subscription-Thread-ID-" + id);
        this.id=id;
        this.client = SubscriptionManager.getSubClient(id);
        SubscriptionManager.addSubSock(id, this);
        this.session=session;
        this.request=client.getRequest();
        this.remoteEndpoint=session.getBasicRemote();
        this.async=session.getAsyncRemote();
        init(session);
    }

    private void init(Session session) {
        try {
            client.startSubscription();
        }
        catch (ParameterException e) {
            String message = "Exception starting subscription.";
            logger.error(message, e);
            sendText(Util.createErrorMessage(e, message));
            close(new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, "Starting subscription failed"));
        }

        sendData();
    }

    private void sendData() {
        future = async.sendText("HANDSHAKE");
        switch(request.getRequestLanguage()){
            case LABVIEW -> sendLVData();
            case PYTHON -> sendPYData();
        }
    }

    private void sendLVData()  {
        while(true){
            try {
                remoteEndpoint.sendBinary(ByteBuffer.wrap(client.getLVData(30).getSecond()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendPYData() {
        boolean end = false;
        do{
            String data;
            try {
                data = client.getPYData(30);
            } catch (InterruptedException e) {
                logger.error("Thread interrupted when retrieving latest value", e);
                return;
            }

            try {
                future.get();
            } catch (InterruptedException e) {
                logger.error("Thread interrupted when sending value", e);
                return;
            } catch (ExecutionException e) {
                logger.error("Thread threw exception when sending latest value", e);
                return;
            } catch(CancellationException e){
                logger.error("Operation cancelled", e);
                return;
            }

            if (data == null){async.sendText("TIMEOUT");}

            else{future = async.sendText(data);}

        }while(true);
    }

    private void sendText(String value)  {
        try {
            remoteEndpoint.sendText(value);
        } catch (IOException e) {
            logger.error("Exception sending text", e);
        }
    }

    @OnMessage
    public void onMessage(String message) throws ExecutionException, InterruptedException {
        if (message.equals("CLOSE")){
            future.get();
            async.sendText("DONE").get();
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
        logger.error("Uncaught exception @onError method executed.", throwable);
        close(new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, "Uncaught exception server side"));
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

    public Thread getThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName)) return t;
        }
        return null;
    }



}
