package cern.rade.services.japc.format;

import cern.japc.core.ValueHeader;
import cern.japc.value.ParameterValue;
import cern.rade.services.codec.CodecException;
import cern.rade.services.japc.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.WebSocketMessage;

import java.io.IOException;
import java.util.Map;

/**
 * One wire format (JSON or LabVIEW): its media type, WebSocket subprotocol, converter, and how it encodes errors and
 * subscription frames.
 */
public abstract class Format {
    /**
     * Reads a request body, refusing one larger than {@code maxBytes} instead of buffering it.
     *
     * @throws ResponseStatusException (413) if the body is larger than {@code maxBytes}
     */
    public static byte[] readBody(HttpInputMessage message, int maxBytes) throws IOException {
        byte[] body = message.getBody().readNBytes(maxBytes + 1);
        if (body.length > maxBytes) {
            throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE, "Request body exceeds " + maxBytes + " bytes");
        }
        return body;
    }

    public abstract MediaType mediaType();

    public abstract String subprotocol();

    public abstract HttpMessageConverter<?> converter(int maxBodyBytes);

    protected HttpHeaders errorHeaders() throws CodecException {
        return new HttpHeaders();
    }

    protected abstract byte[] encodeError(ErrorCode code, String origin, String message) throws CodecException;

    public ResponseEntity<byte[]> errorResponse(HttpStatusCode status, ErrorCode code, String origin, String message) throws CodecException {
        return ResponseEntity.status(status)
                .contentType(mediaType())
                .headers(errorHeaders())
                .body(encodeError(code, origin, message));
    }

    public void writeError(ServerHttpResponse response, HttpStatusCode status, ErrorCode code, String origin, String message) throws CodecException, IOException {
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

    public abstract WebSocketMessage<?> errorFrame(ErrorCode code, String origin, String message) throws CodecException;
}
