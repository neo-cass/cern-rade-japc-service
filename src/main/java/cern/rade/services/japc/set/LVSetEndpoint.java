package cern.rade.services.japc.set;

import cern.japc.core.ParameterException;
import cern.rade.services.codec.CodecException;
import cern.japc.value.ParameterValue;
import cern.rade.services.codec.labview.LVDataDecoder;
import cern.rade.services.codec.labview.LVTypeDecoder;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import cern.rade.services.japc.format.LabVIEWFormat;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.ByteBuffer;
import java.util.Base64;


@RestController
@RequestMapping(value = "/rest", consumes = LabVIEWFormat.MEDIA_TYPE)
public class LVSetEndpoint extends SetEndpoint{
    private LVTypeDescriptor getTypeDescriptor(String type_descriptor) throws CodecException {
        byte[] type_desc = Base64.getUrlDecoder().decode(type_descriptor);
        return LVTypeDecoder.decode(ByteBuffer.wrap(type_desc));
    }

    @PutMapping("/{device}")
    public ResponseEntity<byte[]> setDevice_Type_Descriptor(
            @PathVariable("device") String device,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestHeader(LabVIEWFormat.TYPE_DESCRIPTOR_HEADER) String type_descriptor,
            @RequestHeader(value = "Rbac", required = false) String authStr,
            @RequestBody byte[] data)
            throws ParameterException, CodecException, TokenFormatException, AuthenticationException {

        HttpHeaders retHeaders = new HttpHeaders();
        LVTypeDescriptor lvTypeDescriptor = getTypeDescriptor(type_descriptor);
        ParameterValue value = LVDataDecoder.decode(ByteBuffer.wrap(data), lvTypeDescriptor);
        SetService setService = set(selector, device, lvTypeDescriptor.getName(), value, authStr);

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
