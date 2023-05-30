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
    private int rbac_id;
    private HttpSender sender;

    public SetHandler(HttpServletResponse response, Request request){
        this.request = request;
        this.response = response;
    }
    private void init() throws ParameterException, IOException {
        this.sender=new HttpSender(response);
        this.metaData = request.getMetaData();
        this.data = request.getData();
        this.parameter = JAPCActions.buildParameter(metaData.getProperty(), metaData.getDevice(), null);
        this.selector = SelectorFactory.newSelector(metaData.getSelector());
        if(request.getAuthenticationType() != AuthenticationType.NORMAL){
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

    private void set() throws ParameterException {
        switch(request.getAuthenticationType()){
            case NORMAL -> japcSET();
            case SECURE -> japcSET_SECURE();
        }
    }

    private void japcSET() throws ParameterException {
        ParameterValue pv = JAPCEncoder.encode(data);
        System.out.println(pv);
        parameter.setValue(selector, pv);
    }

    private void japcSET_SECURE() throws ParameterException {
        RbaToken token = rbacClient.getApplicationToken();
        MiddleTierRbaTokenHolder.set(token);
        japcSET();
        MiddleTierRbaTokenHolder.clear();
    }


    private void send() throws IOException {
        if (request.getAuthenticationType() != AuthenticationType.NORMAL){
            MetaData responseMetaData = new MetaData();
            responseMetaData.setRbac_id(rbacClient.hashCode());
            sender.send("ok", metaData);
        }
        else{
            sender.send("ok");
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
            send();
        } catch (IOException e) {
            sender.send(Arrays.toString(e.getStackTrace()));
        }
    }

}

