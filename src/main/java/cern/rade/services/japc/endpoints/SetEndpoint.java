package cern.rade.services.japc.endpoints;

import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.japc.SetService;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rbac.common.TokenFormatException;

public class SetEndpoint extends Endpoint{
    public SetService set(String selector, String device, DataContainer data, String authStr) throws TokenFormatException {
        SetService setService;
        if (authStr != null) {
            RBACClient rbacClient = createRbacClient(authStr);
            setService = new SetService(selector, device, data, rbacClient);
        } else {
            setService = new SetService(selector, device, data);
        }
        return setService;
    }
}
