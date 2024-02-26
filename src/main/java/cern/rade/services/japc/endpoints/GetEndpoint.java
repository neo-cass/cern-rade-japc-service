package cern.rade.services.japc.endpoints;


import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.GetService;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;

public abstract class GetEndpoint {
    private final RBACManager rbacManager = ContextProvider.getRbacManager();
    protected GetService getDevice_Property(String selector, String device, String property, String authStr)
            throws TokenFormatException, AuthenticationException {
        GetService getService;
        if (authStr!=null){
            RBACClient rbacClient = rbacManager.createClient(authStr);
            getService = new GetService(selector, device, property, rbacClient);
        }
        else{
            getService = new GetService(selector, device, property);
        }
        return getService;

    }

    protected GetService getDevice_Property_Field(String selector, String device, String property, String field, String authStr)
            throws TokenFormatException, AuthenticationException {
        GetService getService;
        if (authStr!=null){
            RBACClient rbacClient = rbacManager.createClient(authStr);
            getService = new GetService(selector, device, property, field, rbacClient);
        }
        else{
            getService = new GetService(selector, device, property, field);
        }
        return getService;
    }

    protected GetService getDevice_DataContainer(String selector, String device, DataContainer data, String authStr)
            throws TokenFormatException, AuthenticationException {
        GetService getService;
        if (authStr!=null){
            RBACClient rbacClient = rbacManager.createClient(authStr);
            getService = new GetService(selector, device, data, rbacClient);
        }
        else{
            getService = new GetService(selector, device, data);
        }
        return getService;
    }

}
