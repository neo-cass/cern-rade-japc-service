package cern.rade.services.japc.get;

import cern.japc.core.ParameterException;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.javatolv.LVDataEncoder;
import cern.rade.services.codec.javatolv.LVTypeEncoder;
import cern.rade.services.codec.lvtojava.LVDataContainerFactory;
import cern.rade.services.codec.lvtojava.LVTypeDecoder;
import cern.rade.services.common.BytesUtil;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import cern.rade.services.japc.subscription.SubscriptionInitialiser;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.ByteBuffer;
import java.util.Base64;

@RestController
@RequestMapping(value = "/get", produces = "application/labview-variant")
public class LVGetEndpoint extends GetEndpoint{
    private final Logger logger = LoggerFactory.getLogger(LVGetEndpoint.class);

    private byte[] getLVData(GetService getService) throws CodecException {
        return LVDataEncoder.encode(getService.getData());
    }

    private String getLVType(GetService getService) throws CodecException {
        DataContainer dataContainer = getService.getData();
        byte[] type_desc = BytesUtil.shortArrToByteArr(LVTypeEncoder.encodeTypeDescriptor(dataContainer));
        return Base64.getEncoder().encodeToString(type_desc);
    }

    private DataContainer getDataContainer(String type_descriptor) throws CodecException {
        byte[] type_desc = Base64.getDecoder().decode(type_descriptor);
        LVTypeDescriptor lvTypeDescriptor = LVTypeDecoder.decode(ByteBuffer.wrap(type_desc));
        return LVDataContainerFactory.createDataContainer(lvTypeDescriptor);
    }

    @GetMapping("/{selector}/{device}/{property}")
    public ResponseEntity<byte[]> getDevice_Property_Handle(
            @PathVariable("selector") String selector,
            @PathVariable("device") String device,
            @PathVariable("property") String property,
            @RequestHeader(value = "Rbac", required = false) String authStr)
            throws ParameterException, CodecException, TokenFormatException, AuthenticationException {
        logger.info("Get request for device: " + device + " property: " + property + " selector: " + selector);
        HttpHeaders retHeaders = new HttpHeaders();
        GetService getService = getDevice_Property(selector, device, property, authStr);
        getService.execute();

        retHeaders.set("Type-Descriptor", getLVType(getService));
        if (authStr!=null){
            retHeaders.set("Rbac", "Cached " + getService.getRbacClient().getRbac_id());
        }

        return ResponseEntity
                .ok()
                .headers(retHeaders)
                .body(getLVData(getService));
    }

    @GetMapping(value = "/{selector}/{device}/{property}/{field}")
    public ResponseEntity<byte[]> getDevice_Property_Field_Handle(
            @PathVariable("selector") String selector,
            @PathVariable("device") String device,
            @PathVariable("property") String property,
            @PathVariable("field") String field,
            @RequestHeader(value = "Rbac", required = false) String authStr)
            throws ParameterException, CodecException, TokenFormatException, AuthenticationException {

        HttpHeaders retHeaders = new HttpHeaders();
        GetService getService = getDevice_Property_Field(selector, device, property, field, authStr);
        getService.execute();
        retHeaders.set("Type-Descriptor", getLVType(getService));
        if (authStr!=null){
            retHeaders.set("Rbac", "Cached " + getService.getRbacClient().getRbac_id());
        }
        return ResponseEntity
                .ok()
                .headers(retHeaders)
                .body(getLVData(getService));
    }

    @GetMapping("/{selector}/{device}")
    public ResponseEntity<byte[]> getDevice_Type_Descriptor_Handle(
            @PathVariable("selector") String selector,
            @PathVariable("device") String device,
            @RequestHeader("Type-Descriptor") String type_descriptor,
            @RequestHeader(value = "Rbac", required = false) String authStr)
            throws ParameterException, CodecException, TokenFormatException, AuthenticationException {

        HttpHeaders retHeaders = new HttpHeaders();
        DataContainer dataContainer = getDataContainer(type_descriptor);
        GetService getService = getDevice_DataContainer(selector, device, dataContainer, authStr);
        getService.execute();
        if (authStr!=null){
            retHeaders.set("Rbac", "Cached " + getService.getRbacClient().getRbac_id());
        }
        return ResponseEntity
                .ok()
                .headers(retHeaders)
                .body(getLVData(getService));
    }

    @ExceptionHandler({ParameterException.class, CodecException.class, TokenFormatException.class, AuthenticationException.class})
    public ResponseEntity<String> handleException(Exception e){
        //TODO: generate LabVIEW error cluster
        logger.error("Error in LabVIEW GET endpoint", e);
        return ResponseEntity.badRequest().body(e.getMessage());
    }

}
