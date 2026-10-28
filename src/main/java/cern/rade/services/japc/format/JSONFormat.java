package cern.rade.services.japc.format;

import cern.japc.core.ValueHeader;
import cern.japc.value.ParameterValue;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.json.JSONCodec;
import cern.rade.services.codec.json.JSONErrorEncoder;
import cern.rade.services.japc.error.ErrorCode;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JSONFormat extends Format {
    public static final String MEDIA_TYPE = MediaType.APPLICATION_JSON_VALUE;

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
    protected byte[] encodeError(ErrorCode code, String origin, String message) throws CodecException {
        return JSONErrorEncoder.encode(true, code.value(), "rade-japc " + origin, message).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public WebSocketMessage<?> dataFrame(ParameterValue value, ValueHeader header, Map<String, Object> attributes) throws CodecException {
        String json = JSONCodec.encode(value);
        return new TextMessage("{\"type\":\"data\",\"acqStamp\":" + header.getAcqStamp()
                + ",\"cycleStamp\":" + header.getCycleStamp()
                + ",\"value\":" + json + "}");
    }

    @Override
    public WebSocketMessage<?> errorFrame(ErrorCode code, String origin, String message) throws CodecException {
        return new TextMessage(JSONErrorEncoder.encode("error", true, code.value(), "rade-japc " + origin, message));
    }
}
