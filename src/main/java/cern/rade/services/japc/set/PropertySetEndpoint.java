package cern.rade.services.japc.set;

import cern.japc.core.ParameterException;
import cern.japc.value.ParameterValue;
import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.rbac.RBACHeaders;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({ApiPaths.API, ApiPaths.API_V1})
public class PropertySetEndpoint extends SetEndpoint {
    @PutMapping({ApiPaths.DEVICE_PROPERTY, ApiPaths.DEVICE_PROPERTY_FIELD})
    public ResponseEntity<Void> setDevice_Property(
            @PathVariable("device") String device,
            @PathVariable("property") String property,
            @PathVariable(value = "field", required = false) String field,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestHeader(value = RBACHeaders.HEADER, required = false) String authStr,
            @RequestBody ParameterValue value)
            throws ParameterException, TokenFormatException, AuthenticationException {
        SetService setService = setService(selector, device, property, field, value, authStr);
        setService.execute();
        return ResponseEntity
                .ok()
                .headers(responseHeaders(setService))
                .build();
    }
}
