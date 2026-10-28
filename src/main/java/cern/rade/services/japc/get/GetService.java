package cern.rade.services.japc.get;

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

public class GetService {
    private ParameterValue data;
    private Parameter parameter;
    private Selector selector;
    private AcquiredParameterValue apv;
    private String parameterName;
    private String selectorName;
    private RBACClient rbacClient;
    private long acquisitionStamp;
    private long cycleStamp;
    private boolean executed = false;
    private final Logger logger = LoggerFactory.getLogger(GetService.class);

    public GetService(String selectorName, String device, String property, String field, RBACClient rbacClient){
        this.parameterName = ParameterNames.parameter(device, property, field);
        this.selectorName = ParameterNames.selector(selectorName);
        this.rbacClient = rbacClient;
    }

    private void init() throws ParameterException {
        this.parameter = ParameterFactory.newInstance().newParameter(parameterName);
        this.selector = SelectorFactory.newSelector(selectorName);
    }

    private void get() throws ParameterException, AuthenticationException {
        if (rbacClient==null){
            japcGET();
        }
        else{
            japcGET_SECURE();
        }
    }

    private void japcGET() throws ParameterException {
        this.apv = parameter.getValue(selector);
        this.acquisitionStamp = apv.getHeader().getAcqStamp();
        this.cycleStamp = apv.getHeader().getCycleStamp();
    }

    private void decode() {
        data = apv.getValue();
    }

    private void japcGET_SECURE() throws ParameterException, AuthenticationException {
        RbaToken token = rbacClient.getApplicationToken();
        MiddleTierRbaTokenHolder.set(token);
        japcGET();
        MiddleTierRbaTokenHolder.clear();
    }

    public void execute() throws ParameterException {
        init();
        get();
        decode();
        this.executed = true;
    }

    public boolean isExecuted() {
        return executed;
    }

    public ParameterValue getData() {
        if(!isExecuted()){
            throw new RuntimeException("GetService has not been executed yet. You cant retrieve the data");
        }
        return data;
    }

    public long getAcquisitionStamp() {
        if(!isExecuted()){
            throw new RuntimeException("GetService has not been executed yet. You cant retrieve the acquisition stamp.");
        }
        return acquisitionStamp;
    }

    public long getCycleStamp() {
        if(!isExecuted()){
            throw new RuntimeException("GetService has not been executed yet. You cant retrieve the cycle stamp.");
        }
        return cycleStamp;
    }

    public RBACClient getRbacClient() {
        return rbacClient;
    }
}

