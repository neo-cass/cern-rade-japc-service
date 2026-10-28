package cern.rade.services.japc.get;

import cern.japc.core.ParameterException;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
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
public class LVDescriptorGetEndpoint extends GetEndpoint {
    @GetMapping(value = {"/{device}", "/{device}/{property}", "/{device}/{property}/{field}"}, produces = LabVIEWFormat.MEDIA_TYPE)
    public ResponseEntity<LabVIEWValue> getDevice_Type_Descriptor_Handle(
            @PathVariable("device") String device,
            @PathVariable(value = "property", required = false) String property,
            @PathVariable(value = "field", required = false) String field,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestHeader(LabVIEWFormat.TYPE_DESCRIPTOR_HEADER) String type_descriptor,
            @RequestHeader(value = "Rbac", required = false) String authStr)
            throws ParameterException, CodecException, TokenFormatException, AuthenticationException {
        HttpHeaders retHeaders = new HttpHeaders();
        LVTypeDescriptor lvTypeDescriptor = LabVIEWFormat.matchDescriptor(LabVIEWFormat.typeDescriptorFromHeader(type_descriptor), property, field);
        String propertyName = property == null ? lvTypeDescriptor.getName() : property;
        GetService getService = field == null
                ? getDevice_Property(selector, device, propertyName, authStr)
                : getDevice_Property_Field(selector, device, propertyName, field, authStr);
        getService.execute();
        setStampHeaders(retHeaders, getService);
        if (authStr!=null){
            retHeaders.set("Rbac", "Cached " + getService.getRbacClient().getRbac_id());
        }
        return ResponseEntity
                .ok()
                .headers(retHeaders)
                .body(new LabVIEWValue(getService.getData(), lvTypeDescriptor));
    }
}
