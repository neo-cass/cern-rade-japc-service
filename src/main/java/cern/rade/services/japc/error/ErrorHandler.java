package cern.rade.services.japc.error;

import cern.rade.services.codec.CodecException;
import cern.rade.services.japc.format.Formats;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
    private final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<byte[]> handleException(Exception e, HttpServletRequest request) throws CodecException {
        logger.error("Error handling {} {}", request.getMethod(), request.getRequestURI(), e);
        return Formats.negotiate(request).errorResponse(
                ErrorCode.httpStatus(e),
                ErrorCode.code(e),
                request.getMethod() + " " + request.getRequestURI(),
                ErrorCode.message(e));
    }
}
