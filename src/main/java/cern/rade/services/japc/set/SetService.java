package cern.rade.services.japc.set;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.japc.value.ParameterValue;
import cern.rade.services.japc.ParameterNames;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;
import cern.rbac.util.holder.MiddleTierRbaTokenHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetService {
    private Parameter parameter;
    private Selector selector;
    private String parameterName;
    private String selectorName;
    private final ParameterValue parameterValue;
    private RBACClient rbacClient;
    private boolean isExecuted = false;
    private final Logger logger = LoggerFactory.getLogger(SetService.class);

    public SetService(String selectorName, String device, String property, String field, ParameterValue parameterValue, RBACClient rbacClient){
        this.selectorName = ParameterNames.selector(selectorName);
        this.parameterName = ParameterNames.parameter(device, property, field);
        this.parameterValue = parameterValue;
        this.rbacClient = rbacClient;
    }

    private void init() throws ParameterException {
        this.parameter = ParameterFactory.newInstance().newParameter(parameterName);
        this.selector = SelectorFactory.newSelector(selectorName);
    }

    private void set() throws ParameterException, AuthenticationException {
        if(rbacClient==null){
            japcSET();
        }
        else{
            japcSET_SECURE();
        }
    }

    private void japcSET() throws ParameterException {
        parameter.setValue(selector, parameterValue);
    }

    private void japcSET_SECURE() throws ParameterException, AuthenticationException {
        RbaToken token = rbacClient.getApplicationToken();
        MiddleTierRbaTokenHolder.set(token);
        japcSET();
        MiddleTierRbaTokenHolder.clear();
    }

    public void execute() throws ParameterException, AuthenticationException{
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

