package cern.rade.services.japc.parameter;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.ParameterException;
import cern.japc.value.ParameterValue;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.format.LabVIEWBody;
import cern.rade.services.japc.format.LabVIEWFormat;
import cern.rade.services.japc.format.LabVIEWValue;
import cern.rade.services.japc.format.NamedValue;
import cern.rade.services.japc.security.RbacAuthentication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

/**
 * GET and SET of a device parameter. The property is named in the path (any format) or, for LabVIEW only, by the
 * cluster label of the Lv-Type-Descriptor header.
 */
@RestController
@RequestMapping({ApiPaths.API, ApiPaths.API_V1})
public class ParameterEndpoint {
    public static final String ACQ_STAMP_HEADER = "Acq-Stamp";
    public static final String CYCLE_STAMP_HEADER = "Cycle-Stamp";
    private static final List<MediaType> LABVIEW = List.of(MediaType.parseMediaType(LabVIEWFormat.MEDIA_TYPE));

    private final ParameterService parameters;

    public ParameterEndpoint(ParameterService parameters) {
        this.parameters = parameters;
    }

    @GetMapping({ApiPaths.DEVICE_PROPERTY, ApiPaths.DEVICE_PROPERTY_FIELD})
    public ResponseEntity<NamedValue> get(
            @PathVariable("device") String device,
            @PathVariable("property") String property,
            @PathVariable(value = "field", required = false) String field,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestParam(value = "fields", required = false) List<String> fields)
            throws ParameterException {
        AcquiredParameterValue acquired = parameters.get(new ParameterAddress(device, property, field, selector), RbacAuthentication.required());
        return ResponseEntity
                .ok()
                .headers(responseHeaders(acquired))
                .body(new NamedValue(field == null ? property : field, Fields.select(acquired.getValue(), fields)));
    }

    @GetMapping(value = {ApiPaths.DEVICE, ApiPaths.DEVICE_PROPERTY, ApiPaths.DEVICE_PROPERTY_FIELD},
            headers = LabVIEWFormat.TYPE_DESCRIPTOR_HEADER, produces = LabVIEWFormat.MEDIA_TYPE)
    public ResponseEntity<LabVIEWValue> getByDescriptor(
            @PathVariable("device") String device,
            @PathVariable(value = "property", required = false) String property,
            @PathVariable(value = "field", required = false) String field,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestHeader(LabVIEWFormat.TYPE_DESCRIPTOR_HEADER) String typeDescriptor)
            throws ParameterException, CodecException {
        LVTypeDescriptor lvTypeDescriptor = LabVIEWFormat.matchDescriptor(LabVIEWFormat.typeDescriptorFromHeader(typeDescriptor), property, field);
        ParameterAddress address = new ParameterAddress(device, property == null ? lvTypeDescriptor.getName() : property, field, selector);
        AcquiredParameterValue acquired = parameters.get(address, RbacAuthentication.required());
        return ResponseEntity
                .ok()
                .headers(responseHeaders(acquired))
                .body(new LabVIEWValue(acquired.getValue(), lvTypeDescriptor));
    }

    @PutMapping({ApiPaths.DEVICE_PROPERTY, ApiPaths.DEVICE_PROPERTY_FIELD})
    public ResponseEntity<Void> set(
            @PathVariable("device") String device,
            @PathVariable("property") String property,
            @PathVariable(value = "field", required = false) String field,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestBody ParameterValue value)
            throws ParameterException, CodecException {
        parameters.set(new ParameterAddress(device, property, field, selector), ValueSource.of(value), RbacAuthentication.required());
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = {ApiPaths.DEVICE, ApiPaths.DEVICE_PROPERTY, ApiPaths.DEVICE_PROPERTY_FIELD},
            headers = LabVIEWFormat.TYPE_DESCRIPTOR_HEADER, consumes = LabVIEWFormat.MEDIA_TYPE)
    public ResponseEntity<Void> setByDescriptor(
            @PathVariable("device") String device,
            @PathVariable(value = "property", required = false) String property,
            @PathVariable(value = "field", required = false) String field,
            @RequestParam(value = "selector", required = false) String selector,
            @RequestBody LabVIEWBody body)
            throws ParameterException, CodecException {
        LabVIEWBody value = body.matching(property, field);
        ParameterAddress address = new ParameterAddress(device, property == null ? value.name() : property, field, selector);
        parameters.set(address, descriptor -> value.decode(descriptor.get()), RbacAuthentication.required());
        return ResponseEntity.ok().build();
    }

    // Without these, a device-only request lacking the header would get 406/415 from the LabVIEW route instead of 404.
    @GetMapping(ApiPaths.DEVICE)
    public void getDeviceFallback(@RequestHeader(value = LabVIEWFormat.TYPE_DESCRIPTOR_HEADER, required = false) String typeDescriptor,
                                  HttpServletRequest request) throws Exception {
        if (typeDescriptor == null) {
            throw notFound(request);
        }
        throw new HttpMediaTypeNotAcceptableException(LABVIEW);
    }

    @PutMapping(ApiPaths.DEVICE)
    public void putDeviceFallback(@RequestHeader(value = LabVIEWFormat.TYPE_DESCRIPTOR_HEADER, required = false) String typeDescriptor,
                                  HttpServletRequest request) throws Exception {
        if (typeDescriptor == null) {
            throw notFound(request);
        }
        throw new HttpMediaTypeNotSupportedException(request.getContentType() == null ? null : MediaType.parseMediaType(request.getContentType()), LABVIEW);
    }

    private static NoHandlerFoundException notFound(HttpServletRequest request) {
        return new NoHandlerFoundException(request.getMethod(), request.getRequestURI(), new HttpHeaders());
    }

    private static HttpHeaders responseHeaders(AcquiredParameterValue acquired) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(ACQ_STAMP_HEADER, Long.toString(acquired.getHeader().getAcqStamp()));
        headers.set(CYCLE_STAMP_HEADER, Long.toString(acquired.getHeader().getCycleStamp()));
        return headers;
    }
}
