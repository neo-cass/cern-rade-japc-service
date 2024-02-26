package cern.rade.services.japc.endpoints;

import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.SetService;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.common.TokenFormatException;

public abstract class SetEndpoint{
    private final RBACManager rbacManager = ContextProvider.getRbacManager();
    protected SetService set(String selector, String device, DataContainer data, String authStr) throws TokenFormatException {
        SetService setService;
        if (authStr != null) {
            RBACClient rbacClient = rbacManager.createClient(authStr);
            setService = new SetService(selector, device, data, rbacClient);
        } else {
            setService = new SetService(selector, device, data);
        }
        return setService;
    }
}
