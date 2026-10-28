package cern.rade.services.japc.set;

import cern.japc.core.ParameterException;
import cern.japc.value.ParameterValue;
import cern.rade.services.japc.ApiPaths;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({ApiPaths.API, ApiPaths.API_V1})
public class PropertySetEndpoint extends SetEndpoint {
    @PutMapping({"/{device}/{property}", "/{device}/{property}/{field}"})
    public ResponseEntity<Void> setDevice_Property(
            @PathVariable("device") String device,
            @PathVariable("property") String property,
            @PathVariable(value = "field", required = false) String field,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestHeader(value = "Rbac", required = false) String authStr,
            @RequestBody ParameterValue value)
            throws ParameterException, TokenFormatException, AuthenticationException {
        HttpHeaders retHeaders = new HttpHeaders();
        SetService setService = set(selector, device, property, field, value, authStr);
        setService.execute();
        if(authStr!=null){
            retHeaders.set("Rbac", "Cached " + setService.getRbacClient().getRbac_id());
        }
        return ResponseEntity
                .ok()
                .headers(retHeaders)
                .build();
    }
}
