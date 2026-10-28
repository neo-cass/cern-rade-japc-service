package cern.rade.services.japc.format;

import cern.japc.core.ValueHeader;
import cern.japc.value.ParameterValue;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.json.JSONCodec;
import cern.rade.services.codec.json.JSONErrorEncoder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JSONFormat extends Format {
    public static final String MEDIA_TYPE = MediaType.APPLICATION_JSON_VALUE;
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public MediaType mediaType() {
        return MediaType.APPLICATION_JSON;
    }

    @Override
    public String subprotocol() {
        return "json";
    }

    @Override
    public HttpMessageConverter<?> converter() {
        return new JSONConverter(mediaType());
    }

    @Override
    protected byte[] encodeError(int code, String origin, String message) throws CodecException {
        return error(code, origin, message).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public WebSocketMessage<?> dataFrame(ParameterValue value, ValueHeader header, Map<String, Object> attributes) throws CodecException {
        String json = JSONCodec.encode(value);
        return new TextMessage("{\"type\":\"data\",\"acqStamp\":" + header.getAcqStamp()
                + ",\"cycleStamp\":" + header.getCycleStamp()
                + ",\"value\":" + json + "}");
    }

    @Override
    public WebSocketMessage<?> errorFrame(int code, String origin, String message) throws CodecException {
        try {
            ObjectNode frame = mapper.createObjectNode().put("type", "error");
            frame.setAll((ObjectNode) mapper.readTree(error(code, origin, message)));
            return new TextMessage(mapper.writeValueAsString(frame));
        } catch (JsonProcessingException e) {
            throw new CodecException("Cannot encode error frame to JSON", e);
        }
    }

    private static String error(int code, String origin, String message) throws CodecException {
        return JSONErrorEncoder.encode(true, code, "rade-japc " + origin, message);
    }
}
