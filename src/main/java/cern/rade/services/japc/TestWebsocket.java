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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

@ServerEndpoint(value = "/test/{sessionID}")

public class TestWebsocket {
    private PrintWriter writer;
    @OnOpen
    public void onOpen(Session session, @PathParam("sessionID") String sessionID) throws IOException {
        writer = new PrintWriter(session.getBasicRemote().getSendWriter());
        writer.println("hi");
        writer.close();
//        OutputStreamWriter t = new OutputStreamWriter(session.getBasicRemote().getSendStream());
//        t.write("hi\r\n");

    }


    @OnMessage
    public void onMessage(Session session, String message) throws IOException {

        writer.println("hi");
        writer.close();
    }

    @OnClose
    public void onClose(Session session)  {

    }

    @OnError
    public void onError(Session session, Throwable throwable)  {

    }
}