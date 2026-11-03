package cern.rade.services.japc.set;

import cern.japc.core.Parameter;
import cern.japc.core.ParameterException;
import cern.japc.core.Selector;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.japc.value.ParameterValue;
import cern.rade.services.japc.ParameterNames;
import cern.rade.services.japc.rbac.RbacSession;

public class SetService {
    private Parameter parameter;
    private Selector selector;
    private String parameterName;
    private String selectorName;
    private final ParameterValue parameterValue;
    private RbacSession rbacSession;
    private boolean isExecuted = false;

    public SetService(String selectorName, String device, String property, String field, ParameterValue parameterValue, RbacSession rbacSession){
        this.selectorName = ParameterNames.selector(selectorName);
        this.parameterName = ParameterNames.parameter(device, property, field);
        this.parameterValue = parameterValue;
        this.rbacSession = rbacSession;
    }

    private void init() throws ParameterException {
        this.parameter = ParameterFactory.newInstance().newParameter(parameterName);
        this.selector = SelectorFactory.newSelector(selectorName);
    }

    private void set() throws ParameterException {
        RbacSession.runAs(rbacSession, () -> parameter.setValue(selector, parameterValue));
    }

    public void execute() throws ParameterException {
        init();
        set();
        this.isExecuted = true;
    }

    public boolean isExecuted() {
        return isExecuted;
    }

    public RbacSession getRbacSession() {
        return rbacSession;
    }
}
