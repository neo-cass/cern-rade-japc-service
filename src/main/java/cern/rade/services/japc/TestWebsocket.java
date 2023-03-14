package cern.rade.services.japc;


import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.rade.services.japc.SubscriptionThread;
import cern.rade.services.japc.messages.Request;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.io.Writer;

@ServerEndpoint(value = "/test/{username}/{username2}")

public class TestWebsocket {

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username, @PathParam("username2") String username2) {
        System.out.println(username);
        System.out.println(username2);
    }


    @OnMessage
    public void onMessage(Session session, String message) throws IOException {

    }

    @OnClose
    public void onClose(Session session)  {

    }

    @OnError
    public void onError(Session session, Throwable throwable)  {

    }
}
