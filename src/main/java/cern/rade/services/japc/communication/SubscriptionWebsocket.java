package cern.rade.services.japc.communication;
import cern.rade.services.japc.handlers.SubscriptionThread;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;

@ServerEndpoint(value = "/subs/{username}_{property}_{selector}")
public class SubscriptionWebsocket {
    String username;
    String property;
    String selector;
    SubscriptionThread st;
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username, @PathParam("property") String property, @PathParam("selector") String selector) throws IOException {
        this.username=username;
        this.property=property;
        this.selector = selector;
        this.st = new SubscriptionThread();
        st.start();
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        if(message.equals("END_SESSION")){session.close();}
    }

    @OnClose
    public void onClose(Session session) throws IOException {

    }

    @OnError
    public void onError(Session session, Throwable throwable) {

    }
}
