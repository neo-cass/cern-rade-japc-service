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
import cern.rade.services.common.communication.RequestAction;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.japc.examples.JAPCActions;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACClientFactory;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;
import cern.rbac.util.holder.MiddleTierRbaTokenHolder;
import jakarta.servlet.http.HttpServletResponse;

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

    public SetHandler(HttpServletResponse response, Request request){
        this.request = request;
        this.response = response;
    }
    private void init() throws ParameterException, IOException {
        this.sender= new HttpSender(response);
        this.metaData = request.getMetaData();
        this.data = request.getData();
        this.parameter = JAPCActions.buildParameter(metaData.getProperty(), metaData.getDevice(), null);
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


    private void respond() throws IOException {
        switch(request.getAuthenticationType()){
            case NONE -> sender.send("ok");
            case RBAC_CACHED, RBAC_EXPLICIT, RBAC_LOCATION -> {
                MetaData responseMetaData = new MetaData();
                responseMetaData.setRbac_id(rbacClient.getRbac_id());
                sender.send("ok", responseMetaData);
            }
        }
    }
    public void execute(){
        try {
            init();
        } catch (ParameterException | IOException e) {
            sender.send(Arrays.toString(e.getStackTrace()));
            return;
        }

        try {
            set();
        } catch (ParameterException e) {
            sender.send(Arrays.toString(e.getStackTrace()));
            return;
        }

        try {
            respond();
        } catch (IOException e) {
            sender.send(Arrays.toString(e.getStackTrace()));
        }
    }

}

