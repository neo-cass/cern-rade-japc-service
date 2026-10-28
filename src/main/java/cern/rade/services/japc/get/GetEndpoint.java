package cern.rade.services.japc.get;


import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.HttpHeaders;

public abstract class GetEndpoint {
    public static final String ACQ_STAMP_HEADER = "Acq-Stamp";
    public static final String CYCLE_STAMP_HEADER = "Cycle-Stamp";
    private final RBACManager rbacManager = ContextProvider.getRbacManager();

    protected void setStampHeaders(HttpHeaders headers, GetService getService) {
        headers.set(ACQ_STAMP_HEADER, Long.toString(getService.getAcquisitionStamp()));
        headers.set(CYCLE_STAMP_HEADER, Long.toString(getService.getCycleStamp()));
    }

    protected GetService getDevice_Property(String selector, String device, String property, String authStr)
            throws TokenFormatException, AuthenticationException {
        GetService getService;
        if (authStr!=null){
            RBACClient rbacClient = rbacManager.createClient(authStr);
            getService = new GetService(cleanSelector(selector), device, property, rbacClient);
        }
        else{
            getService = new GetService(cleanSelector(selector), device, property);
        }
        return getService;

    }

    protected GetService getDevice_Property_Field(String selector, String device, String property, String field, String authStr)
            throws TokenFormatException, AuthenticationException {
        GetService getService;
        if (authStr!=null){
            RBACClient rbacClient = rbacManager.createClient(authStr);
            getService = new GetService(cleanSelector(selector), device, property, field, rbacClient);
        }
        else{
            getService = new GetService(cleanSelector(selector), device, property, field);
        }
        return getService;
    }

    protected String cleanSelector(String selector){
        selector = selector == null ? "" : selector;
        return selector;
    }
}
