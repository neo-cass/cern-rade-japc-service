package cern.rade.services.japc.set;

import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.common.TokenFormatException;

public abstract class SetEndpoint{
    private final RBACManager rbacManager = ContextProvider.getRbacManager();
    protected SetService set(String selector, String device, DataContainer data, String authStr) throws TokenFormatException {
        SetService setService;
        if (authStr != null) {
            RBACClient rbacClient = rbacManager.createClient(authStr);
            setService = new SetService(cleanSelector(selector), device, data, rbacClient);
        } else {
            setService = new SetService(cleanSelector(selector), device, data);
        }
        return setService;
    }
    protected String cleanSelector(String selector){
        selector = selector.equals("NONE") ? "" : selector;
        return selector;
    }
}
