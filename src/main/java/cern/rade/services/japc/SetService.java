package cern.rade.services.japc;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.japc.value.ParameterValue;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.japcjava.JAPCDecoder;
import cern.rade.services.codec.japcjava.JAPCEncoder;
import cern.rade.services.common.communication.AuthenticationType;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;
import cern.rbac.util.holder.MiddleTierRbaTokenHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetService {
    private DataContainer data;
    private Parameter parameter;
    private Selector selector;
    private String parameterName;
    private String selectorName;
    private RBACClient rbacClient;
    private boolean isExecuted = false;
    private final Logger logger = LoggerFactory.getLogger(SetService.class);

    public SetService(String selectorName, String device, DataContainer data, RBACClient rbacClient){
        this.selectorName =selectorName;
        this.parameterName = Parameters.buildParameterName(device, data.getName());
        this.data = data;
        this.rbacClient = rbacClient;
    }

    public SetService(String selectorName, String device, DataContainer data){
        this.selectorName = selectorName;
        this.parameterName = Parameters.buildParameterName(device, data.getName());
        this.data = data;
    }

    private void init() throws ParameterException {
        this.parameter = ParameterFactory.newInstance().newParameter(parameterName);
        this.selector = SelectorFactory.newSelector(selectorName);
    }

    private void set() throws ParameterException, CodecException, AuthenticationException {
        if(rbacClient==null){
            japcSET();
        }
        else{
            japcSET_SECURE();
        }
    }

    private void japcSET() throws ParameterException, CodecException {
        ParameterValue pv = JAPCEncoder.encode(data);
        parameter.setValue(selector, pv);
    }

    private void japcSET_SECURE() throws ParameterException, AuthenticationException, CodecException {
        RbaToken token = rbacClient.getApplicationToken();
        MiddleTierRbaTokenHolder.set(token);
        japcSET();
        MiddleTierRbaTokenHolder.clear();
    }

    public void execute() throws ParameterException, CodecException, AuthenticationException{
        init();
        set();
        this.isExecuted = true;
    }

    public boolean isExecuted() {
        return isExecuted;
    }

    public RBACClient getRbacClient() {
        return rbacClient;
    }
}

