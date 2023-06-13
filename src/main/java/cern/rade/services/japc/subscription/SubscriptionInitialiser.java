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
    private SubscriptionHandler handler;

    public SubscriptionInitialiser(Request request, HttpServletResponse response){
        this.request=request;
        this.response=response;
    }

    private void init() throws IOException, ParameterException {
        this.sender = new HttpSender(response);
        this.handler = new SubscriptionHandler(request);
        handler.init();
        SubscriptionManager.addSubClient(handler.getSub_id(), handler);
    }



    private void send(){
        int sub_id = handler.getSub_id();
        switch(request.getAuthenticationType()){
            case NONE -> sender.send(String.valueOf(sub_id));
            case RBAC_LOCATION, RBAC_CACHED, RBAC_EXPLICIT -> {
                MetaData metaData = new MetaData();
                metaData.setRbac_id(handler.getRbac_id());
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
