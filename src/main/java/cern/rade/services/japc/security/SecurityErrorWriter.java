package cern.rade.services.japc.security;

import cern.rade.services.codec.CodecException;
import cern.rade.services.japc.error.ErrorCode;
import cern.rade.services.japc.error.ErrorLog;
import cern.rade.services.japc.format.Format;
import cern.rade.services.japc.format.Formats;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;

import java.io.IOException;

/**
 * Writes 401 and 403 responses in the format the client asked for, like every other error of the service.
 */
public final class SecurityErrorWriter {
    private static final Logger logger = LoggerFactory.getLogger(SecurityErrorWriter.class);

    private SecurityErrorWriter() {
    }

    public static AuthenticationEntryPoint unauthorized(String message) {
        return (request, response, exception) -> write(request, response, HttpStatus.UNAUTHORIZED, message);
    }

    public static AccessDeniedHandler forbidden(String message) {
        return (request, response, exception) -> write(request, response, HttpStatus.FORBIDDEN, message);
    }

    public static void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        String origin = request.getMethod() + " " + request.getRequestURI();
        ErrorLog.log(logger, origin, status, message, null);
        try {
            ResponseEntity<byte[]> error = format(request).errorResponse(status, ErrorCode.RBAC_ERROR, origin, message);
            response.setStatus(status.value());
            error.getHeaders().forEach((name, values) -> values.forEach(value -> response.addHeader(name, value)));
            response.getOutputStream().write(error.getBody());
        } catch (CodecException e) {
            throw new IOException("Could not encode error response", e);
        }
    }

    private static Format format(HttpServletRequest request) {
        if (request.getHeader(WebSocketHttpHeaders.SEC_WEBSOCKET_PROTOCOL) == null) {
            return Formats.negotiate(request);
        }
        Format format = Formats.forHandshake(new ServletServerHttpRequest(request).getHeaders());
        return format == null ? Formats.preferred() : format;
    }
}
