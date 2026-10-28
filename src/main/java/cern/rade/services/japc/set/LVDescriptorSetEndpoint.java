package cern.rade.services.japc.set;

import cern.japc.core.ParameterException;
import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.format.LabVIEWFormat;
import cern.rade.services.japc.format.LabVIEWValue;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = {ApiPaths.API, ApiPaths.API_V1}, headers = LabVIEWFormat.TYPE_DESCRIPTOR_HEADER)
public class LVDescriptorSetEndpoint extends SetEndpoint {
    @PutMapping(value = {"/{device}", "/{device}/{property}", "/{device}/{property}/{field}"}, consumes = LabVIEWFormat.MEDIA_TYPE)
    public ResponseEntity<Void> setDevice_Type_Descriptor(
            @PathVariable("device") String device,
            @PathVariable(value = "property", required = false) String property,
            @PathVariable(value = "field", required = false) String field,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestHeader(value = "Rbac", required = false) String authStr,
            @RequestBody LabVIEWValue body)
            throws ParameterException, TokenFormatException, AuthenticationException {
        HttpHeaders retHeaders = new HttpHeaders();
        LabVIEWValue value = LabVIEWFormat.matchValue(body, property, field);
        String propertyName = property == null ? value.descriptor().getName() : property;
        SetService setService = set(selector, device, propertyName, field, value.value(), authStr);
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
