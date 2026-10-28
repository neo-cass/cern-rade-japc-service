package cern.rade.services.japc.set;

import cern.japc.value.ParameterValue;
import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.common.TokenFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SetEndpoint{
    private final Logger logger = LoggerFactory.getLogger(SetEndpoint.class);
    private final RBACManager rbacManager = ContextProvider.getRbacManager();
    protected SetService set(String selector, String device, String property, ParameterValue data, String authStr) throws TokenFormatException {
        SetService setService;
        if (authStr != null) {
            RBACClient rbacClient = rbacManager.createClient(authStr);
            setService = new SetService(cleanSelector(selector), device, property, data, rbacClient);
        } else {
            setService = new SetService(cleanSelector(selector), device, property, data);
        }
        return setService;
    }
    protected SetService set(String selector, String device, String property, String field, ParameterValue data, String authStr) throws TokenFormatException {
        if (field == null) {
            return set(selector, device, property, data, authStr);
        }
        SetService setService;
        if (authStr != null) {
            RBACClient rbacClient = rbacManager.createClient(authStr);
            setService = new SetService(cleanSelector(selector), device, property, field, data, rbacClient);
        } else {
            setService = new SetService(cleanSelector(selector), device, property, field, data);
        }
        return setService;
    }
    protected String cleanSelector(String selector){
        selector = selector == null ? "" : selector;
        return selector;
    }
}
