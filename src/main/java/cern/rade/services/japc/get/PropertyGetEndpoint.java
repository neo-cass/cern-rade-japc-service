package cern.rade.services.japc.get;

import cern.japc.core.ParameterException;
import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.Fields;
import cern.rade.services.japc.format.NamedValue;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({ApiPaths.API, ApiPaths.API_V1})
public class PropertyGetEndpoint extends GetEndpoint {
    @GetMapping({"/{device}/{property}", "/{device}/{property}/{field}"})
    public ResponseEntity<NamedValue> getDevice_Property_Handle(
            @PathVariable("device") String device,
            @PathVariable("property") String property,
            @PathVariable(value = "field", required = false) String field,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestParam(value = "fields", required = false) List<String> fields,
            @RequestHeader(value = "Rbac", required = false) String authStr)
            throws ParameterException, TokenFormatException, AuthenticationException {
        HttpHeaders retHeaders = new HttpHeaders();
        GetService getService = field == null
                ? getDevice_Property(selector, device, property, authStr)
                : getDevice_Property_Field(selector, device, property, field, authStr);
        getService.execute();
        setStampHeaders(retHeaders, getService);
        if (authStr!=null){
            retHeaders.set("Rbac", "Cached " + getService.getRbacClient().getRbac_id());
        }
        return ResponseEntity
                .ok()
                .headers(retHeaders)
                .body(new NamedValue(field == null ? property : field, Fields.select(getService.getData(), fields)));
    }
}
