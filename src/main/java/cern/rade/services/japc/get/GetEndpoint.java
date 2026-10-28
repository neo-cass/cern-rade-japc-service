package cern.rade.services.japc.get;

import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.rbac.RBACHeaders;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.HttpHeaders;

public abstract class GetEndpoint {
    public static final String ACQ_STAMP_HEADER = "Acq-Stamp";
    public static final String CYCLE_STAMP_HEADER = "Cycle-Stamp";
    private final RBACManager rbacManager = ContextProvider.getRbacManager();

    protected GetService getService(String selector, String device, String property, String field, String authStr)
            throws TokenFormatException, AuthenticationException {
        return new GetService(selector, device, property, field, rbacManager.optionalClient(authStr));
    }

    protected HttpHeaders responseHeaders(GetService getService) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(ACQ_STAMP_HEADER, Long.toString(getService.getAcquisitionStamp()));
        headers.set(CYCLE_STAMP_HEADER, Long.toString(getService.getCycleStamp()));
        RBACHeaders.setCached(headers, getService.getRbacClient());
        return headers;
    }
}
