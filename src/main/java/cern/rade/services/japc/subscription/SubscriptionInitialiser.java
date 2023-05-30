package cern.rade.services.japc.subscription;

import cern.japc.core.ParameterException;
import cern.rade.services.common.communication.AuthenticationType;
import cern.rade.services.common.communication.MetaData;
import cern.rade.services.common.communication.Request;
import cern.rade.services.japc.HttpSender;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.client.authentication.AuthenticationException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

public class SubscriptionInitialiser {
    private Request request;
    private HttpSender sender;
    private HttpServletResponse response;
    private int rbac_id;
    private int sub_id;
    private RBACClient rbacClient;
    private MetaData metaData;

    public SubscriptionInitialiser(Request request, HttpServletResponse response){
        this.request=request;
        this.metaData=request.getMetaData();
        this.response=response;
    }

    private void init() throws IOException, ParameterException {
        this.sender = new HttpSender(response);
        SubscriptionHandler handler = new SubscriptionHandler(request);
        handler.init();
        SubscriptionManager.addSubClient(handler.hashCode(), handler);
        this.sub_id=handler.hashCode();
        if(request.getAuthenticationType() !=AuthenticationType.NORMAL){
            initRBAC();
        }
    }

    private void initRBAC() {
        this.rbac_id = metaData.getRbac_id();
        this.rbacClient = RBACManager.getClient(rbac_id);
        if(rbac_id == 0 || rbacClient==null){
            initNewRBAC();
        }
    }

    private void initNewRBAC() {
        try{
            this.rbacClient = new RBACClient(metaData.getUsername(), metaData.getPassword());
            RBACManager.addClient(rbacClient);
        }
        catch (AuthenticationException e){
            sender.send("auth failed");
        }
    }

    private void send(){
        if (request.getAuthenticationType()!=AuthenticationType.NORMAL){
            MetaData metaData = new MetaData();
            metaData.setRbac_id(rbac_id);
            sender.send(String.valueOf(sub_id), metaData);
        }
        else{
            sender.send(String.valueOf(rbac_id));
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
