package cern.rade.services.japc.set;

import cern.japc.core.ParameterException;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.lvtojava.LVDecoder;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.ByteBuffer;
import java.util.Base64;


@RestController
@RequestMapping(value = "/set", consumes = "application/labview-variant")
public class LVSetEndpoint extends SetEndpoint{
    private DataContainer getDataContainer(byte[] data, String type_descriptor) throws CodecException {
        byte[] type_desc = Base64.getUrlDecoder().decode(type_descriptor);
        return LVDecoder.decode(ByteBuffer.wrap(type_desc), ByteBuffer.wrap(data));
    }

    @PutMapping("/{selector}/{device}")
    public ResponseEntity<byte[]> setDevice_Type_Descriptor(
            @PathVariable("selector") String selector,
            @PathVariable("device") String device,
            @RequestHeader("Type-Descriptor") String type_descriptor,
            @RequestHeader(value = "Rbac", required = false) String authStr,
            @RequestBody byte[] data)
            throws ParameterException, CodecException, TokenFormatException, AuthenticationException {

        HttpHeaders retHeaders = new HttpHeaders();
        DataContainer dataContainer = getDataContainer(data, type_descriptor);
        SetService setService = set(selector, device, dataContainer, authStr);

        setService.execute();
        if(authStr!=null){
            retHeaders.set("Rbac", "Cached " + setService.getRbacClient().getRbac_id());
        }
        return ResponseEntity
                .ok()
                .headers(retHeaders)
                .build();
    }

    @ExceptionHandler({ParameterException.class, CodecException.class, TokenFormatException.class, AuthenticationException.class})
    public ResponseEntity<String> handleException(Exception e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
