package cern.rade.services.japc.get;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.japc.value.ParameterValue;
import cern.rade.services.japc.ParameterNames;
import cern.rade.services.japc.rbac.RbacSession;

public class GetService {
    private ParameterValue data;
    private Parameter parameter;
    private Selector selector;
    private AcquiredParameterValue apv;
    private String parameterName;
    private String selectorName;
    private RbacSession rbacSession;
    private long acquisitionStamp;
    private long cycleStamp;
    private boolean executed = false;

    public GetService(String selectorName, String device, String property, String field, RbacSession rbacSession){
        this.parameterName = ParameterNames.parameter(device, property, field);
        this.selectorName = ParameterNames.selector(selectorName);
        this.rbacSession = rbacSession;
    }

    private void init() throws ParameterException {
        this.parameter = ParameterFactory.newInstance().newParameter(parameterName);
        this.selector = SelectorFactory.newSelector(selectorName);
    }

    private void get() throws ParameterException {
        this.apv = RbacSession.callAs(rbacSession, () -> parameter.getValue(selector));
        this.acquisitionStamp = apv.getHeader().getAcqStamp();
        this.cycleStamp = apv.getHeader().getCycleStamp();
    }

    private void decode() {
        data = apv.getValue();
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
}
