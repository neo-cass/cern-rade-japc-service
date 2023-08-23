package cern.rade.services.japc;

import cern.japc.core.Parameter;
import cern.japc.core.ParameterException;
import cern.japc.core.Selector;
import cern.japc.core.factory.SelectorFactory;
import cern.japc.value.ParameterValue;
import cern.rade.services.codec.japcjava.JAPCEncoder;
import cern.rade.services.common.communication.AuthenticationType;
import cern.rade.services.common.communication.MetaData;
import cern.rade.services.common.communication.Request;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACClientFactory;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;
import cern.rbac.util.holder.MiddleTierRbaTokenHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

public class SetHandler {
    private HttpServletResponse response;
    private Request request;
    private DataContainer data;
    private MetaData metaData;
    private Parameter parameter;
    private Selector selector;
    private RBACClient rbacClient;
    private HttpSender sender;
    private Logger logger = LoggerFactory.getLogger(SetHandler.class);

    public SetHandler(HttpServletResponse response, Request request){
        this.request = request;
        this.response = response;
    }

    private void init() throws ParameterException, IOException {
        this.sender= new HttpSender(response);
        this.metaData = request.getMetaData();
        this.data = request.getData();
        this.parameter = Util.buildParameter(metaData.getProperty(), metaData.getDevice(), null);
        this.selector = SelectorFactory.newSelector(metaData.getSelector());
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


    private void set() throws ParameterException {
        switch(request.getAuthenticationType()){
            case NONE -> japcSET();
            case RBAC_CACHED, RBAC_EXPLICIT, RBAC_LOCATION -> japcSET_SECURE();
        }
    }

    private void japcSET() throws ParameterException {
        ParameterValue pv = JAPCEncoder.encode(data);
        parameter.setValue(selector, pv);
    }

    private void japcSET_SECURE() throws ParameterException {
        RbaToken token = rbacClient.getApplicationToken();
        MiddleTierRbaTokenHolder.set(token);
        japcSET();
        MiddleTierRbaTokenHolder.clear();
    }


    private boolean respond() {
        switch(request.getAuthenticationType()){
            case NONE -> {
                return sender.send("OK");
            }
            case RBAC_CACHED, RBAC_EXPLICIT, RBAC_LOCATION -> {
                MetaData responseMetaData = new MetaData();
                responseMetaData.setRbac_id(rbacClient.getRbac_id());
                return sender.send("OK", responseMetaData);
            }
        }
        //TODO: why does java force return at the end of this if every case is covered
        return false;
    }
    public void execute(){
        try {
            init();
        } catch (ParameterException | IOException e) {
            sender.send("ERROR: " + Arrays.toString(e.getStackTrace()));
            logger.error("Error handling set operation for ID: ", e);
            RBACManager.removeRBACClient(rbacClient.getRbac_id());
            return;
        }

        try {
            set();
        } catch (ParameterException e) {
            sender.send("ERROR: " + Arrays.toString(e.getStackTrace()));
            RBACManager.removeRBACClient(rbacClient.getRbac_id());
            return;
        }

        //respond and if send fails remove the rbac client
        if (!respond()){RBACManager.removeRBACClient(rbacClient.getRbac_id());}
    }

}

