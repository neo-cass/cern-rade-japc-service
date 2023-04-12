package cern.rade.services.japc;
import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.rade.services.common.types.JavaContainer;
import cern.rade.services.japc.messages.MetaData;
import cern.rade.services.japc.messages.Request;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Arrays;

@ServerEndpoint(value = "/subs/{id}")
public class SubscriptionWebsocket {
    private int id;
    private RemoteEndpoint.Basic writer;
    private SubscriptionClient client;
    private Session session;

    @OnOpen
    public void onOpen(Session session, @PathParam("id") int id) {
        this.id=id;
        this.client = SubscriptionManager.getSubClient(id);
        this.session=session;
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
                writer.sendText(Arrays.toString(e.getStackTrace()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        sendValues();
    }

    private void sendValues(){
        while(session.isOpen()){
            try {
                writer.sendText(client.getLatest());
            } catch (IOException | InterruptedException e) {
                System.out.println("Connection closed");
            }
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        if(message.equals("END_SESSION")){close(session);}
        if(message.equals("IS_OPEN")){writer.sendText("YES");}
    }

    @OnClose
    public void onClose(Session session)  {
        close(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable)  {
        throwable.printStackTrace();
    }

    private void close(Session session){
        SubscriptionManager.removeSubSock(id);
        try {
            session.close();
        } catch (IOException e) {
            System.out.println("Error while ending session. Here is the stack trace:\n");
            e.printStackTrace();
        }
    }
}
