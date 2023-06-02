package cern.rade.services.japc.subscription;
import cern.japc.core.*;
import cern.rade.services.codec.japcjava.JAPCDecoder;
import cern.rade.services.codec.javatolv.LVTypeEncoder;
import cern.rade.services.common.communication.Request;
import cern.rade.services.common.containers.java.PropertyContainer;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.*;
import java.nio.ByteBuffer;

@ServerEndpoint(value = "/subs/{id}")
public class SubscriptionWebsocket {
    private int id;
    private RemoteEndpoint.Basic writer;
    private SubscriptionHandler client;
    private Session session;
    private OutputStreamWriter out;
    private Request request;

    @OnOpen
    public void onOpen(Session session, @PathParam("id") int id) {
        this.id=id;
        this.client = SubscriptionManager.getSubClient(id);
        this.session=session;
        this.request=client.getRequest();
        Thread.currentThread().setName("Subscription-Thread-"+client.getSub_id());
        init(session);
    }

    private void init(Session session) {
        writer = session.getBasicRemote();
        try {
            client.startSubscription();
        }
        catch (ParameterException e) {
            sendMessage("Failed to create parameter. Please check device, property, field, CCDE and that you are on the correct network.");
        }
        try {
            sendData();
        } catch (IOException | InterruptedException e) {
            sendMessage("TODO");
        }
    }

    private void sendData() throws IOException, InterruptedException {
        switch(request.getRequestLanguage()){
            case LABVIEW -> sendLVData();
            case PYTHON -> sendPYData();
        }
    }

    private void sendLVData() throws IOException, InterruptedException {
        while(session.isOpen() && !Thread.currentThread().isInterrupted()){
            writer.sendBinary(ByteBuffer.wrap(client.getLVData(30).getSecond()));
        }
    }

    private void sendPYData() throws InterruptedException, IOException {
        while(session.isOpen() && !Thread.currentThread().isInterrupted()){
            writer.sendText(client.getPYData(30));
        }
    }

    private void sendMessage(String value)  {
        try {
            out = new OutputStreamWriter(session.getBasicRemote().getSendStream());
            out.write(value);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnMessage
    public void onMessage(String message) throws IOException {
        if(message.equals("CLOSE_SUBSCRIPTION")){
            client.stopSubscription();
        }
    }

    @OnClose
    public void onClose()  {
        close();
    }

    @OnError
    public void onError(Session session, Throwable throwable)  {
        ;
    }

    public void close(){
        getThreadByName("Subscription-Thread-"+client.getSub_id()).interrupt();
        SubscriptionManager.removeSubSock(id);
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Deliberate disconnect"));
        } catch (IOException e) {
            System.out.println("Error while ending session. Here is the stack trace:\n");
            e.printStackTrace();
        }
    }

    public Thread getThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName)) return t;
        }
        return null;
    }

}
