package cern.rade.services.japc;

import cern.japc.core.Parameter;
import cern.japc.core.ParameterException;
import cern.japc.core.Selector;
import cern.japc.core.factory.SelectorFactory;
import cern.japc.value.ParameterValue;
import cern.rade.services.codec.japcjava.JAPCEncoder;
import cern.rade.services.common.communication.client.MetaData;
import cern.rade.services.common.communication.client.Request;
import cern.rade.services.common.communication.client.RequestAction;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.japc.examples.JAPCActions;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rbac.common.RbaToken;
import cern.rbac.util.holder.MiddleTierRbaTokenHolder;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class SetClient {
    private HttpServletResponse response;
    private Request request;
    private DataContainer data;
    private MetaData metaData;
    private Parameter parameter;
    private Selector selector;
    private RBACClient rbacClient;

    public SetClient(HttpServletResponse response, Request request){
        this.request = request;
        this.response = response;
    }
    private void init() throws ParameterException {
        this.metaData = request.getMetaData();
        if(request.getRequestAction()== RequestAction.GET_SECURE){
            this.rbacClient = new RBACClient(metaData.getUsername(), metaData.getPassword());
        }
        this.data = request.getData();
        this.parameter = JAPCActions.buildParameter(metaData.getProperty(), metaData.getDevice(), null);
        this.selector = SelectorFactory.newSelector(metaData.getSelector());
    }
    private void set() throws ParameterException {
        switch(request.getRequestAction()){
            case SET -> japcSET();
            case SET_SECURE -> japcSET_SECURE();
        }
    }

    private void japcSET() throws ParameterException {
        ParameterValue pv = JAPCEncoder.encode(data);
        System.out.println(pv);
        parameter.setValue(selector, pv);
    }

    private void japcSET_SECURE() throws ParameterException {
        RbaToken token = rbacClient.login();
        MiddleTierRbaTokenHolder.set(token);
        japcSET();
        MiddleTierRbaTokenHolder.clear();
    }


    private void send() throws IOException {
        PrintWriter writer = response.getWriter();
        writer.write("ok");
        writer.flush();
        writer.close();
    }
    public void execute(){
        try {
            init();
        } catch (ParameterException e) {
            throw new RuntimeException(e);
        }

        try {
            set();
        } catch (ParameterException e) {
            throw new RuntimeException(e);
        }

        try {
            send();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

