package cern.rade.services.japc.get;

import cern.japc.core.ParameterException;
import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.Fields;
import cern.rade.services.japc.format.NamedValue;
import cern.rade.services.japc.rbac.RBACHeaders;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({ApiPaths.API, ApiPaths.API_V1})
public class PropertyGetEndpoint extends GetEndpoint {
    @GetMapping({ApiPaths.DEVICE_PROPERTY, ApiPaths.DEVICE_PROPERTY_FIELD})
    public ResponseEntity<NamedValue> getDevice_Property_Handle(
            @PathVariable("device") String device,
            @PathVariable("property") String property,
            @PathVariable(value = "field", required = false) String field,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestParam(value = "fields", required = false) List<String> fields,
            @RequestHeader(value = RBACHeaders.HEADER, required = false) String authStr)
            throws ParameterException, TokenFormatException, AuthenticationException {
        GetService getService = getService(selector, device, property, field, authStr);
        getService.execute();
        return ResponseEntity
                .ok()
                .headers(responseHeaders(getService))
                .body(new NamedValue(field == null ? property : field, Fields.select(getService.getData(), fields)));
    }
}
