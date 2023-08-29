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
import cern.rbac.common.TokenFormatException;
import cern.rbac.util.holder.MiddleTierRbaTokenHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

public class SetHandler {
    private HttpServletResponse response;
    private Client client;
    private DataContainer data;
    private MetaData responseMetaData;
    private Parameter parameter;
    private Selector selector;
    private RBACClient rbacClient;
    private HttpSender sender;
    private Logger logger = LoggerFactory.getLogger(SetHandler.class);

    public SetHandler(HttpServletResponse response, Client client){
        this.client = client;
        this.response = response;
        this.responseMetaData  = new MetaData();
    }

    private void init() throws IOException {
        this.sender= new HttpSender(response);
        this.data = client.getDataContainer();
        this.parameter = client.getParameter();
        this.selector = client.getSelector();
        this.rbacClient=client.getRbacClient();
    }

    private void set() throws ParameterException {
        if(rbacClient == null){
            japcSET();
        }
        else{
            japcSET_SECURE();
        }
    }

    private void japcSET() throws ParameterException {
        ParameterValue pv = JAPCEncoder.encode(data);
        parameter.setValue(selector, pv);
    }

    private void japcSET_SECURE() throws ParameterException, AuthenticationException {
        RbaToken token = rbacClient.getApplicationToken();
        MiddleTierRbaTokenHolder.set(token);
        japcSET();
        MiddleTierRbaTokenHolder.clear();
    }

    private boolean respond() {
        if(client.getAuthenticationType()==AuthenticationType.NONE){
            return sender.send("SUCCESS");
        }
        else{
            responseMetaData.setRbac_id(rbacClient.getRbac_id());
            return sender.send("SUCCESS", responseMetaData);
        }
    }
    public void execute(){
        try {
            init();
        } catch (IOException e) {
            logger.error("Error handling set operation.", e);
            return;
        }

        try {
            set();
        } catch (ParameterException e) {
            responseMetaData.setError(e.getMessage());
            sender.send("FAILURE", responseMetaData);
            logger.error("Failure to do set operation", e);
            return;
        } catch (AuthenticationException e){
            responseMetaData.setError(e.getMessage());
            sender.send("FAILURE", responseMetaData);
            RBACManager.removeRBACClient(rbacClient.getRbac_id());
            logger.error("Failed to authenticate with RBAC");
            return;
        }

        //respond and if send fails (and the user is not using already cached rbac) remove the rbac client
        if (!respond() && client.getAuthenticationType() != AuthenticationType.RBAC_CACHED){
            RBACManager.removeRBACClient(rbacClient.getRbac_id());
        }
    }

}

