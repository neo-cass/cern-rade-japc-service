package cern.rade.services.japc.communication;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;


@ServerEndpoint(value = "/subs/{sessionID}")

public class SubscriptionEndpoint {
    public int sessionID;
    @OnOpen
    public void onOpen(Session session, @PathParam("sessionID") int sessionID) throws IOException {
        System.out.println(sessionID);
        this.sessionID = sessionID;
        while(true){session.getBasicRemote().sendText("hi");}

    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        System.out.println(message);
    }

    @OnClose
    public void onClose(Session session) throws IOException {

    }

    @OnError
    public void onError(Session session, Throwable throwable) {

    }
}
