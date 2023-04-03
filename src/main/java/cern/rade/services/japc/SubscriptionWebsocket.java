package cern.rade.services.japc;
import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.rade.services.common.types.JavaContainer;
import cern.rade.services.japc.SubscriptionThread;
import cern.rade.services.japc.messages.MetaData;
import cern.rade.services.japc.messages.Request;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

@ServerEndpoint(value = "/subs/{sessionID}")
public class SubscriptionWebsocket {
    private int sessionID;
    private String username;
    private String selector;
    private String device;
    private String propertyName;
    private Request request;
    private MetaData metaData;
    private JavaContainer<?> property;
    private RemoteEndpoint.Basic writer;
    private SubscriptionThread st;
    @OnOpen
    public void onOpen(Session session, @PathParam("sessionID") int sessionID) {
        this.sessionID=sessionID;
        this.request = SubscriptionHandler.getRequest(sessionID);
        this.metaData=request.getMetaData();
        this.username=metaData.username;
        this.device = metaData.device;
        this.selector = metaData.selector;
        this.property=request.getData();
        this.propertyName=property.getFieldName();
        init(session);
    }

    private void init(Session session) {
        SubscriptionHandler.addSub(sessionID, this);
        SubscriptionHandler.removeRequest(sessionID);
        String name = Parameters.buildParameterName(device, propertyName);

        Parameter parameter = null;
        try {
            parameter = ParameterFactory.newInstance().newParameter(name);
        } catch (ParameterException e) {
            e.printStackTrace();
        }

        writer = session.getBasicRemote();

        ValueCallback vc = new ValueCallback();
        SubscriptionHandle sh;
        Selector selectorObj;
        if(!selector.equals("NO_SELECTOR")){selectorObj = SelectorFactory.newSelector(selector);}
        else{selectorObj = SelectorFactory.newSelector("");}
        sh = parameter.createSubscription(selectorObj, vc);
        this.st = new SubscriptionThread(sh, vc, writer);
        st.start();
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        if(message.equals("END_SESSION")){close(session);}
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
        st.interrupt();
        SubscriptionHandler.removeSub(sessionID);
        try {
            session.close();
        } catch (IOException e) {
            System.out.println("Error while ending session. Here is the stack trace:\n");
            e.printStackTrace();
        }
    }
}
