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

        try{
            while(true){
                String data=client.getPYData(30);

                if (data == null){

                    remoteEndpoint.sendPing(ByteBuffer.wrap("".getBytes()));
                }
                else{
                    remoteEndpoint.sendText(data);
                }

            }
        } catch (IOException e1) {
            logger.debug("Client disconnected");
        } catch (InterruptedException e2){
            logger.debug("Thread interrupted, stopping subscription");
        }

    }

    private void sendText(String value)  {
        try {
            remoteEndpoint.sendText(value);
        } catch (IOException e) {
            logger.error("Exception sending text", e);
        }
    }

    private void sendTextAsync(String value)  {
        Future<Void> f = async.sendText(value);
    }


    @OnMessage
    public void onMessage(String message) {}

    @OnClose
    public void onClose()  {
        close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Deliberate disconnect"));
    }

    @OnError
    public void onError(Session session, Throwable throwable)  {
        logger.error("Uncaught exception @onError method executed.", throwable);
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
