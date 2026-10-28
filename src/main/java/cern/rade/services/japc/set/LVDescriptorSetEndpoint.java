package cern.rade.services.japc.set;

import cern.japc.core.ParameterException;
import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.format.LabVIEWFormat;
import cern.rade.services.japc.format.LabVIEWValue;
import cern.rade.services.japc.rbac.RBACHeaders;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = {ApiPaths.API, ApiPaths.API_V1}, headers = LabVIEWFormat.TYPE_DESCRIPTOR_HEADER)
public class LVDescriptorSetEndpoint extends SetEndpoint {
    @PutMapping(value = {ApiPaths.DEVICE, ApiPaths.DEVICE_PROPERTY, ApiPaths.DEVICE_PROPERTY_FIELD}, consumes = LabVIEWFormat.MEDIA_TYPE)
    public ResponseEntity<Void> setDevice_Type_Descriptor(
            @PathVariable("device") String device,
            @PathVariable(value = "property", required = false) String property,
            @PathVariable(value = "field", required = false) String field,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestHeader(value = RBACHeaders.HEADER, required = false) String authStr,
            @RequestBody LabVIEWValue body)
            throws ParameterException, TokenFormatException, AuthenticationException {
        LabVIEWValue value = LabVIEWFormat.matchValue(body, property, field);
        SetService setService = setService(selector, device, property == null ? value.descriptor().getName() : property, field, value.value(), authStr);
        setService.execute();
        return ResponseEntity
                .ok()
                .headers(responseHeaders(setService))
                .build();
    }
}
