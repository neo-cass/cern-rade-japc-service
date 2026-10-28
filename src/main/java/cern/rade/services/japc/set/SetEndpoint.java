package cern.rade.services.japc.set;

import cern.japc.value.ParameterValue;
import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.rbac.RBACHeaders;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.HttpHeaders;

public abstract class SetEndpoint{
    private final RBACManager rbacManager = ContextProvider.getRbacManager();

    protected SetService setService(String selector, String device, String property, String field, ParameterValue data, String authStr)
            throws TokenFormatException, AuthenticationException {
        return new SetService(selector, device, property, field, data, rbacManager.optionalClient(authStr));
    }

    protected HttpHeaders responseHeaders(SetService setService) {
        HttpHeaders headers = new HttpHeaders();
        RBACHeaders.setCached(headers, setService.getRbacClient());
        return headers;
    }
}
