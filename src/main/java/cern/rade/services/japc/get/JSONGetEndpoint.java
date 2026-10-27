package cern.rade.services.japc.get;

import cern.japc.core.ParameterException;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.json.JSONCodec;
import cern.rade.services.japc.format.JSONFormat;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/rest", produces = JSONFormat.MEDIA_TYPE)
public class JSONGetEndpoint extends GetEndpoint{
    @GetMapping("/{device}/{property}")
    public ResponseEntity<String> getDevice_Property_Handle(
            @PathVariable("device") String device,
            @PathVariable("property") String property,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestParam(value = "fields", required = false) List<String> fields,
            @RequestHeader(value = "Rbac", required = false) String authStr)
            throws ParameterException, CodecException, TokenFormatException, AuthenticationException {
        HttpHeaders retHeaders = new HttpHeaders();
        GetService getService = getDevice_Property(selector, device, property, authStr);
        getService.execute();
        if (authStr!=null){
            retHeaders.set("Rbac", "Cached " + getService.getRbacClient().getRbac_id());
        }
        return ResponseEntity
                .ok()
                .headers(retHeaders)
                .body(JSONCodec.encode(getService.getData(), fields));
    }

    @GetMapping("/{device}/{property}/{field}")
    public ResponseEntity<String> getDevice_Property_Field_Handle(
            @PathVariable("device") String device,
            @PathVariable("property") String property,
            @PathVariable("field") String field,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestHeader(value = "Rbac", required = false) String authStr)
            throws ParameterException, CodecException, TokenFormatException, AuthenticationException {
        HttpHeaders retHeaders = new HttpHeaders();
        GetService getService = getDevice_Property_Field(selector, device, property, field, authStr);
        getService.execute();
        if (authStr!=null){
            retHeaders.set("Rbac", "Cached " + getService.getRbacClient().getRbac_id());
        }
        return ResponseEntity
                .ok()
                .headers(retHeaders)
                .body(JSONCodec.encode(getService.getData()));
    }
}
