package cern.rade.services.japc.set;

import cern.japc.core.ParameterException;
import cern.japc.value.ParameterValue;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.json.JSONCodec;
import cern.rade.services.japc.format.JSONFormat;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/rest", consumes = JSONFormat.MEDIA_TYPE)
public class JSONSetEndpoint extends SetEndpoint{
    @PutMapping("/{device}/{property}")
    public ResponseEntity<byte[]> setDevice_Property(
            @PathVariable("device") String device,
            @PathVariable("property") String property,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestHeader(value = "Rbac", required = false) String authStr,
            @RequestBody String data)
            throws ParameterException, CodecException, TokenFormatException, AuthenticationException {

        HttpHeaders retHeaders = new HttpHeaders();
        ParameterValue value = JSONCodec.decode(data);
        SetService setService = set(selector, device, property, value, authStr);

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
