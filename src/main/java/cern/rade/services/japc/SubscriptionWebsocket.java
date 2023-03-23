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

@ServerEndpoint(value = "/subs/{username}/{device}/{property}/{selector}")
public class SubscriptionWebsocket {
    private String username;
    private String property;
    private String selector;
    private String device;
    private Request request;
    private RemoteEndpoint.Basic writer;
    private SubscriptionThread st;
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username, @PathParam("device") String device, @PathParam("property") String property, @PathParam("selector") String selector) {
        this.username=username;
        this.device = device;
        this.property=property;
        this.selector = selector;
        this.request = SubscriptionHandler.getRequest(username+"/"+device+"/"+property+"/"+selector);

        init(session);
    }

    private void init(Session session){
        SubscriptionHandler.addSub(this);
        String name = Parameters.buildParameterName(device, property);

        Parameter parameter = null;
        try {
            parameter = ParameterFactory.newInstance().newParameter(name);
        } catch (ParameterException e) {
            throw new RuntimeException(e);
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
        if(message.equals("END_SESSION")){close(session, null);}
    }

    @OnClose
    public void onClose(Session session)  {
        close(session, null);
    }

    @OnError
    public void onError(Session session, Throwable throwable)  {
        close(session, throwable);
    }

    private void close(Session session, Throwable throwable){
        if(throwable!=null){throwable.printStackTrace();}
        st.interrupt();
        SubscriptionHandler.removeSub(this);
        SubscriptionHandler.removeRequest(request.id);
        try {
            session.close();
        } catch (IOException e) {
            System.out.println("Error while ending session. Here is the stack trace:\n");
            e.printStackTrace();
        }
    }
}
