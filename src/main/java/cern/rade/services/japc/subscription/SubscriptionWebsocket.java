package cern.rade.services.japc.subscription;
import cern.japc.core.*;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.commons.lang3.CharSet;

import java.io.*;
import java.util.Arrays;

@ServerEndpoint(value = "/subs/{id}")
public class SubscriptionWebsocket {
    private int id;
    private RemoteEndpoint.Basic writer;
    private SubscriptionClient client;
    private Session session;
    private OutputStreamWriter out;

    @OnOpen
    public void onOpen(Session session, @PathParam("id") int id) {
        this.id=id;
        this.client = SubscriptionManager.getSubClient(id);
        this.session=session;
        Thread.currentThread().setName("Subscription-Thread-"+client.getSessionID());
        init(session);
    }

    private void init(Session session) {
        writer = session.getBasicRemote();
        try {
            client.startSubscription();
        }
        catch (ParameterException e) {
            e.printStackTrace();
            try {
                writer.sendText("Failed to create parameter. Please check device, property, field, CCDE and that you are on the correct network.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        sendData();
    }

    private void sendData(){
        while(session.isOpen() && !Thread.currentThread().isInterrupted()){
            AcquiredParameterValue val = null;
            try {
                val = client.getLatest(30);
            } catch (InterruptedException e) {
                return;
            }
            try {
                //TODO: send in parts
                writer.sendText(String.valueOf(val));

            } catch (IOException e) {
                System.out.println("Failed to send latest value due to IOException.");
            }
        }
        client.stopSubscription();
    }

    private void sendData(String value) throws IOException {
        out = new OutputStreamWriter(session.getBasicRemote().getSendStream());
        out.write(value);
        out.close();
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {

    }

    @OnClose
    public void onClose(Session session)  {
        close(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable)  {
        ;
    }

    private void close(Session session){
        getThreadByName("Subscription-Thread-"+client.getSessionID()).interrupt();
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
