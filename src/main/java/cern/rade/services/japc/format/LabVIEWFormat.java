package cern.rade.services.japc.format;

import cern.japc.core.ValueHeader;
import cern.japc.value.ParameterValue;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.labview.LVDataEncoder;
import cern.rade.services.codec.labview.LVErrorEncoder;
import cern.rade.services.codec.labview.LVTypeDecoder;
import cern.rade.services.codec.labview.LVTypeEncoder;
import cern.rade.services.common.BytesUtil;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import cern.rade.services.japc.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketMessage;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;

public class LabVIEWFormat extends Format {
    public static final String MEDIA_TYPE = "application/labview-variant";
    public static final String TYPE_DESCRIPTOR_HEADER = "Lv-Type-Descriptor";
    public static final byte DATA_MESSAGE = 0x00;
    public static final byte ERROR_MESSAGE = 0x01;
    private static final String TYPE_DESCRIPTOR_ATTRIBUTE = "typeDescriptor";

    @Override
    public MediaType mediaType() {
        return MediaType.parseMediaType(MEDIA_TYPE);
    }

    @Override
    public String subprotocol() {
        return "labview-variant";
    }

    @Override
    public HttpMessageConverter<?> converter() {
        return new LabVIEWConverter(mediaType());
    }

    @Override
    protected HttpHeaders errorHeaders() throws CodecException {
        HttpHeaders headers = new HttpHeaders();
        headers.set(TYPE_DESCRIPTOR_HEADER, typeDescriptorToHeader(LVErrorEncoder.encodeTypeDescriptor()));
        return headers;
    }

    @Override
    protected byte[] encodeError(ErrorCode code, String origin, String message) throws CodecException {
        return LVErrorEncoder.encode(true, code.value(), "rade-japc " + origin + "<ERR>" + message);
    }

    @Override
    public String propertyFromHeaders(HttpHeaders headers) throws CodecException {
        String typeDescriptor = headers.getFirst(TYPE_DESCRIPTOR_HEADER);
        return typeDescriptor == null ? null : typeDescriptorFromHeader(typeDescriptor).getName();
    }

    @Override
    public void prepareSubscription(HttpServletRequest request, HttpHeaders responseHeaders, String property, String field, ParameterValue latest, Map<String, Object> attributes) throws CodecException {
        String typeDescriptor = request.getHeader(TYPE_DESCRIPTOR_HEADER);
        LVTypeDescriptor lvTypeDescriptor;
        if (typeDescriptor != null) {
            lvTypeDescriptor = matchDescriptor(typeDescriptorFromHeader(typeDescriptor), property, field);
        } else {
            lvTypeDescriptor = LVTypeEncoder.describe(field != null ? field : property, latest);
            responseHeaders.set(TYPE_DESCRIPTOR_HEADER, typeDescriptorToHeader(LVTypeEncoder.encodeTypeDescriptor(lvTypeDescriptor, latest)));
        }
        attributes.put(TYPE_DESCRIPTOR_ATTRIBUTE, lvTypeDescriptor);
    }

    @Override
    public WebSocketMessage<?> dataFrame(ParameterValue value, ValueHeader header, Map<String, Object> attributes) throws CodecException {
        byte[] data = LVDataEncoder.encode(value, (LVTypeDescriptor) attributes.get(TYPE_DESCRIPTOR_ATTRIBUTE));
        return new BinaryMessage(ByteBuffer.allocate(data.length + 17)
                .put(DATA_MESSAGE)
                .putLong(header.getAcqStamp())
                .putLong(header.getCycleStamp())
                .put(data)
                .flip());
    }

    @Override
    public WebSocketMessage<?> errorFrame(ErrorCode code, String origin, String message) throws CodecException {
        byte[] error = encodeError(code, origin, message);
        return new BinaryMessage(ByteBuffer.allocate(error.length + 1).put(ERROR_MESSAGE).put(error).flip());
    }

    public static LVTypeDescriptor matchDescriptor(LVTypeDescriptor descriptor, String property, String field) {
        boolean cluster = descriptor.getType() == LVType.CLUSTER;
        if (field == null) {
            if (cluster && (property == null || property.equals(descriptor.getName()))) {
                return descriptor;
            }
        } else if (cluster) {
            LVTypeDescriptor[] fields = ((LVClusterDescriptor) descriptor).getFieldDescriptors();
            if (descriptor.getName().equals(property) && fields.length == 1 && field.equals(fields[0].getName())) {
                return fields[0];
            }
        } else if (field.equals(descriptor.getName())) {
            return descriptor;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TYPE_DESCRIPTOR_HEADER + " '" + descriptor.getName()
                + "' does not match " + (property == null ? "a property" : property) + (field == null ? "" : "/" + field));
    }

    public static LabVIEWValue matchValue(LabVIEWValue labviewValue, String property, String field) {
        LVTypeDescriptor descriptor = matchDescriptor(labviewValue.descriptor(), property, field);
        if (descriptor == labviewValue.descriptor()) {
            return labviewValue;
        }
        return new LabVIEWValue(labviewValue.value().castAsMap().get(descriptor.getName()), descriptor);
    }

    public static String typeDescriptorToHeader(short[] typeDescriptor) {
        return Base64.getUrlEncoder().encodeToString(BytesUtil.shortArrToByteArr(typeDescriptor));
    }

    public static LVTypeDescriptor typeDescriptorFromHeader(String typeDescriptor) throws CodecException {
        try {
            return LVTypeDecoder.decode(ByteBuffer.wrap(Base64.getUrlDecoder().decode(typeDescriptor)));
        } catch (IllegalArgumentException e) {
            throw new CodecException(TYPE_DESCRIPTOR_HEADER + " is not valid base64url", e);
        }
    }
}
