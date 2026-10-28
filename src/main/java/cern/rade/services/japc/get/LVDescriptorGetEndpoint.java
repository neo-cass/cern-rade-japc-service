package cern.rade.services.japc.get;

import cern.japc.core.ParameterException;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
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
public class LVDescriptorGetEndpoint extends GetEndpoint {
    @GetMapping(value = {ApiPaths.DEVICE, ApiPaths.DEVICE_PROPERTY, ApiPaths.DEVICE_PROPERTY_FIELD}, produces = LabVIEWFormat.MEDIA_TYPE)
    public ResponseEntity<LabVIEWValue> getDevice_Type_Descriptor_Handle(
            @PathVariable("device") String device,
            @PathVariable(value = "property", required = false) String property,
            @PathVariable(value = "field", required = false) String field,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestHeader(LabVIEWFormat.TYPE_DESCRIPTOR_HEADER) String type_descriptor,
            @RequestHeader(value = RBACHeaders.HEADER, required = false) String authStr)
            throws ParameterException, CodecException, TokenFormatException, AuthenticationException {
        LVTypeDescriptor lvTypeDescriptor = LabVIEWFormat.matchDescriptor(LabVIEWFormat.typeDescriptorFromHeader(type_descriptor), property, field);
        GetService getService = getService(selector, device, property == null ? lvTypeDescriptor.getName() : property, field, authStr);
        getService.execute();
        return ResponseEntity
                .ok()
                .headers(responseHeaders(getService))
                .body(new LabVIEWValue(getService.getData(), lvTypeDescriptor));
    }
}
