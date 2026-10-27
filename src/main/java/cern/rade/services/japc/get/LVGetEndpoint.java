package cern.rade.services.japc.get;

import cern.japc.core.ParameterException;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.labview.LVDataEncoder;
import cern.rade.services.codec.labview.LVTypeEncoder;
import cern.rade.services.codec.labview.LVTypeDecoder;
import cern.rade.services.common.BytesUtil;
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
@RequestMapping(value = "/rest", produces = LabVIEWFormat.MEDIA_TYPE)
public class LVGetEndpoint extends GetEndpoint{

    private String getLVType(LVTypeDescriptor lvTypeDescriptor, GetService getService) throws CodecException {
        byte[] type_desc = BytesUtil.shortArrToByteArr(LVTypeEncoder.encodeTypeDescriptor(lvTypeDescriptor, getService.getData()));
        return Base64.getUrlEncoder().encodeToString(type_desc);
    }

    private LVTypeDescriptor getTypeDescriptor(String type_descriptor) throws CodecException {
        byte[] type_desc = Base64.getUrlDecoder().decode(type_descriptor);
        return LVTypeDecoder.decode(ByteBuffer.wrap(type_desc));
    }

    @GetMapping("/{device}/{property}")
    public ResponseEntity<byte[]> getDevice_Property_Handle(
            @PathVariable("device") String device,
            @PathVariable("property") String property,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestHeader(value = "Rbac", required = false) String authStr)
            throws ParameterException, CodecException, TokenFormatException, AuthenticationException {
        HttpHeaders retHeaders = new HttpHeaders();
        GetService getService = getDevice_Property(selector, device, property, authStr);
        getService.execute();
        LVTypeDescriptor lvTypeDescriptor = LVTypeEncoder.describe(property, getService.getData());

        retHeaders.set(LabVIEWFormat.TYPE_DESCRIPTOR_HEADER, getLVType(lvTypeDescriptor, getService));
        if (authStr!=null){
            retHeaders.set("Rbac", "Cached " + getService.getRbacClient().getRbac_id());
        }

        return ResponseEntity
                .ok()
                .headers(retHeaders)
                .body(LVDataEncoder.encode(getService.getData(), lvTypeDescriptor));
    }

    @GetMapping(value = "/{device}/{property}/{field}")
    public ResponseEntity<byte[]> getDevice_Property_Field_Handle(
            @PathVariable("device") String device,
            @PathVariable("property") String property,
            @PathVariable("field") String field,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestHeader(value = "Rbac", required = false) String authStr)
            throws ParameterException, CodecException, TokenFormatException, AuthenticationException {

        HttpHeaders retHeaders = new HttpHeaders();
        GetService getService = getDevice_Property_Field(selector, device, property, field, authStr);
        getService.execute();
        LVTypeDescriptor lvTypeDescriptor = LVTypeEncoder.describe(field, getService.getData());
        retHeaders.set(LabVIEWFormat.TYPE_DESCRIPTOR_HEADER, getLVType(lvTypeDescriptor, getService));
        if (authStr!=null){
            retHeaders.set("Rbac", "Cached " + getService.getRbacClient().getRbac_id());
        }
        return ResponseEntity
                .ok()
                .headers(retHeaders)
                .body(LVDataEncoder.encode(getService.getData(), lvTypeDescriptor));
    }

    @GetMapping("/{device}")
    public ResponseEntity<byte[]> getDevice_Type_Descriptor_Handle(
            @PathVariable("device") String device,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestHeader(LabVIEWFormat.TYPE_DESCRIPTOR_HEADER) String type_descriptor,
            @RequestHeader(value = "Rbac", required = false) String authStr)
            throws ParameterException, CodecException, TokenFormatException, AuthenticationException {

        HttpHeaders retHeaders = new HttpHeaders();
        LVTypeDescriptor lvTypeDescriptor = getTypeDescriptor(type_descriptor);
        GetService getService = getDevice_Property(selector, device, lvTypeDescriptor.getName(), authStr);
        getService.execute();
        if (authStr!=null){
            retHeaders.set("Rbac", "Cached " + getService.getRbacClient().getRbac_id());
        }
        return ResponseEntity
                .ok()
                .headers(retHeaders)
                .body(LVDataEncoder.encode(getService.getData(), lvTypeDescriptor));
    }
}
