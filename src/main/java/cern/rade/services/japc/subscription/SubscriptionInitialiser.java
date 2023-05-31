package cern.rade.services.japc.subscription;

import cern.japc.core.ParameterException;
import cern.rade.services.common.communication.AuthenticationType;
import cern.rade.services.common.communication.MetaData;
import cern.rade.services.common.communication.Request;
import cern.rade.services.japc.HttpSender;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACClientFactory;
import cern.rbac.client.authentication.AuthenticationException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

public class SubscriptionInitialiser {
    private Request request;
    private HttpSender sender;
    private HttpServletResponse response;
    private RBACClient rbacClient;
    private MetaData metaData;
    private int sub_id;

    public SubscriptionInitialiser(Request request, HttpServletResponse response){
        this.request=request;
        this.metaData=request.getMetaData();
        this.response=response;
    }

    private void init() throws IOException, ParameterException {
        this.sender = new HttpSender(response);
        SubscriptionHandler handler = new SubscriptionHandler(request);
        handler.init();
        this.sub_id=handler.getSub_id();
        SubscriptionManager.addSubClient(handler.getSub_id(), handler);
        if(request.getAuthenticationType()!=AuthenticationType.NONE){initRBAC();}
    }

    private void initRBAC() {
        try{
            this.rbacClient = new RBACClientFactory(request).createClient();
        }
        catch (AuthenticationException e){
            sender.send(Arrays.toString(e.getStackTrace()));
        }
    }

    private void send(){
        switch(request.getAuthenticationType()){
            case NONE -> sender.send(String.valueOf(sub_id));
            case RBAC_LOCATION, RBAC_CACHED, RBAC_EXPLICIT -> {
                MetaData metaData = new MetaData();
                metaData.setRbac_id(rbacClient.getRbac_id());
                sender.send(String.valueOf(sub_id), metaData);
            }
        }
    }

    public void execute() {
        try {
            init();
        } catch (IOException | ParameterException e) {
            sender.send(Arrays.toString(e.getStackTrace()));
            return;
        }
        send();
    }
}
