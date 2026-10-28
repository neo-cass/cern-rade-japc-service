package cern.rade.services.japc.get;

import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.format.LabVIEWFormat;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

@RestController
@RequestMapping({ApiPaths.API, ApiPaths.API_V1})
public class LVDescriptorFallbackEndpoint {
    private static final List<MediaType> LABVIEW = List.of(MediaType.parseMediaType(LabVIEWFormat.MEDIA_TYPE));

    @GetMapping(ApiPaths.DEVICE)
    public void get(@RequestHeader(value = LabVIEWFormat.TYPE_DESCRIPTOR_HEADER, required = false) String typeDescriptor,
                    HttpServletRequest request) throws Exception {
        if (typeDescriptor == null) {
            throw notFound(request);
        }
        throw new HttpMediaTypeNotAcceptableException(LABVIEW);
    }

    @PutMapping(ApiPaths.DEVICE)
    public void put(@RequestHeader(value = LabVIEWFormat.TYPE_DESCRIPTOR_HEADER, required = false) String typeDescriptor,
                    HttpServletRequest request) throws Exception {
        if (typeDescriptor == null) {
            throw notFound(request);
        }
        throw new HttpMediaTypeNotSupportedException(request.getContentType() == null ? null : MediaType.parseMediaType(request.getContentType()), LABVIEW);
    }

    private static NoHandlerFoundException notFound(HttpServletRequest request) {
        return new NoHandlerFoundException(request.getMethod(), request.getRequestURI(), new HttpHeaders());
    }
}
