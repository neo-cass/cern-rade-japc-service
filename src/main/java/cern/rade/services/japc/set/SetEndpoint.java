package cern.rade.services.japc.set;

import cern.japc.value.ParameterValue;
import cern.rade.services.japc.rbac.RbacHeaders;
import cern.rade.services.japc.security.RbacAuthentication;
import org.springframework.http.HttpHeaders;

public abstract class SetEndpoint {

    protected SetService setService(String selector, String device, String property, String field, ParameterValue data) {
        return new SetService(selector, device, property, field, data, RbacAuthentication.current().orElse(null));
    }

    protected HttpHeaders responseHeaders(SetService setService) {
        HttpHeaders headers = new HttpHeaders();
        RbacHeaders.setCached(headers, setService.getRbacSession());
        return headers;
    }
}
