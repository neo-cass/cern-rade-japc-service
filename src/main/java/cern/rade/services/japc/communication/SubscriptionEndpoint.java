package cern.rade.services.japc.communication;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;


@ServerEndpoint(value = "/subs")

public class SubscriptionEndpoint {
    @OnOpen
    public void onOpen(Session session) throws IOException {
        System.out.println("hi");
        session.getBasicRemote().sendText("hi");

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
