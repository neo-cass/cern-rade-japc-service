package cern.rade.services.japc.set;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.japc.value.ParameterValue;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.japcjava.JAPCEncoder;
import cern.rade.services.common.containers.java.DataContainer;
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
    private ParameterValue parameterValue;
    private RBACClient rbacClient;
    private boolean isExecuted = false;
    private final Logger logger = LoggerFactory.getLogger(SetService.class);

    public SetService(String selectorName, String device, DataContainer data){
        this.selectorName = selectorName;
        this.parameterName = Parameters.buildParameterName(device, data.getName());
        this.data = data;
    }

    public SetService(String selectorName, String device, DataContainer data, RBACClient rbacClient){
        this(selectorName, device, data);
        this.rbacClient = rbacClient;
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

    private void encode() throws CodecException {
        parameterValue = JAPCEncoder.encode(data);
    }

    private void japcSET() throws ParameterException, CodecException {
        parameter.setValue(selector, parameterValue);
    }

    private void japcSET_SECURE() throws ParameterException, AuthenticationException, CodecException {
        RbaToken token = rbacClient.getApplicationToken();
        MiddleTierRbaTokenHolder.set(token);
        japcSET();
        MiddleTierRbaTokenHolder.clear();
    }

    public void execute() throws ParameterException, CodecException, AuthenticationException{
        encode();
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

