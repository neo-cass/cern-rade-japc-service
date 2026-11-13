package cern.rade.services.japc.set;

import cern.japc.core.ParameterException;
import cern.japc.value.ParameterValue;
import cern.rade.services.codec.CodecException;
import cern.rade.services.japc.ApiPaths;
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
            @RequestBody ParameterValue value)
            throws ParameterException, CodecException {
        SetService setService = setService(selector, device, property, field, value);
        setService.execute();
        return ResponseEntity.ok().build();
    }
}
