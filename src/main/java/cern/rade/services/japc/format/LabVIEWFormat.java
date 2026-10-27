package cern.rade.services.japc.format;

import cern.japc.value.ParameterValue;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.labview.LVDataEncoder;
import cern.rade.services.codec.labview.LVErrorEncoder;
import cern.rade.services.codec.labview.LVTypeDecoder;
import cern.rade.services.codec.labview.LVTypeEncoder;
import cern.rade.services.common.BytesUtil;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    @Override
    public MediaType mediaType() {
        return MediaType.parseMediaType(MEDIA_TYPE);
    }

    @Override
    public String subprotocol() {
        return "labview-variant";
    }

    @Override
    protected HttpHeaders errorHeaders() throws CodecException {
        HttpHeaders headers = new HttpHeaders();
        headers.set(TYPE_DESCRIPTOR_HEADER, toHeader(LVErrorEncoder.encodeTypeDescriptor()));
        return headers;
    }

    @Override
    protected byte[] encodeError(int code, String origin, String message) throws CodecException {
        return LVErrorEncoder.encode(true, code, "rade-japc " + origin + "<ERR>" + message);
    }

    @Override
    public String propertyFromHeaders(HttpHeaders headers) throws CodecException {
        String typeDescriptor = headers.getFirst(TYPE_DESCRIPTOR_HEADER);
        return typeDescriptor == null ? null : fromHeader(typeDescriptor).getName();
    }

    @Override
    public void prepareSubscription(HttpServletRequest request, HttpHeaders responseHeaders, String name, ParameterValue latest, Map<String, Object> attributes) throws CodecException {
        String typeDescriptor = request.getHeader(TYPE_DESCRIPTOR_HEADER);
        LVTypeDescriptor lvTypeDescriptor;
        if (typeDescriptor != null) {
            lvTypeDescriptor = fromHeader(typeDescriptor);
        } else {
            lvTypeDescriptor = LVTypeEncoder.describe(name, latest);
            responseHeaders.set(TYPE_DESCRIPTOR_HEADER, toHeader(LVTypeEncoder.encodeTypeDescriptor(lvTypeDescriptor, latest)));
        }
        attributes.put("typeDescriptor", lvTypeDescriptor);
    }

    @Override
    public WebSocketMessage<?> dataFrame(ParameterValue value, Map<String, Object> attributes) throws CodecException {
        return frame(DATA_MESSAGE, LVDataEncoder.encode(value, (LVTypeDescriptor) attributes.get("typeDescriptor")));
    }

    @Override
    public WebSocketMessage<?> errorFrame(int code, String origin, String message) throws CodecException {
        return frame(ERROR_MESSAGE, encodeError(code, origin, message));
    }

    private static BinaryMessage frame(byte messageType, byte[] payload) {
        return new BinaryMessage(ByteBuffer.allocate(payload.length + 1).put(messageType).put(payload).flip());
    }

    private static String toHeader(short[] typeDescriptor) {
        return Base64.getUrlEncoder().encodeToString(BytesUtil.shortArrToByteArr(typeDescriptor));
    }

    private static LVTypeDescriptor fromHeader(String typeDescriptor) throws CodecException {
        return LVTypeDecoder.decode(ByteBuffer.wrap(Base64.getUrlDecoder().decode(typeDescriptor)));
    }
}
