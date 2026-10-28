package cern.rade.services.japc.format;

import cern.japc.core.ValueHeader;
import cern.japc.value.ParameterValue;
import cern.rade.services.codec.CodecException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketMessage;

import java.io.IOException;
import java.util.Map;

public abstract class Format {
    public abstract MediaType mediaType();

    public abstract String subprotocol();

    public abstract HttpMessageConverter<?> converter();

    protected HttpHeaders errorHeaders() throws CodecException {
        return new HttpHeaders();
    }

    protected abstract byte[] encodeError(int code, String origin, String message) throws CodecException;

    public ResponseEntity<byte[]> errorResponse(HttpStatusCode status, int code, String origin, String message) throws CodecException {
        return ResponseEntity.status(status)
                .contentType(mediaType())
                .headers(errorHeaders())
                .body(encodeError(code, origin, message));
    }

    public void writeError(ServerHttpResponse response, HttpStatusCode status, int code, String origin, String message) throws CodecException, IOException {
        response.setStatusCode(status);
        response.getHeaders().setContentType(mediaType());
        response.getHeaders().addAll(errorHeaders());
        response.getBody().write(encodeError(code, origin, message));
    }

    public String propertyFromHeaders(HttpHeaders headers) throws CodecException {
        return null;
    }

    public void prepareSubscription(HttpServletRequest request, HttpHeaders responseHeaders, String property, String field, ParameterValue latest, Map<String, Object> attributes) throws CodecException {
    }

    public abstract WebSocketMessage<?> dataFrame(ParameterValue value, ValueHeader header, Map<String, Object> attributes) throws CodecException;

    public abstract WebSocketMessage<?> errorFrame(int code, String origin, String message) throws CodecException;
}
