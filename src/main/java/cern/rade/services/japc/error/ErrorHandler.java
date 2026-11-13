package cern.rade.services.japc.error;

import cern.rade.services.codec.CodecException;
import cern.rade.services.japc.format.Formats;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
    private final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<byte[]> handleException(Exception e, HttpServletRequest request) throws CodecException {
        HttpStatusCode status = ErrorCode.httpStatus(e);
        String origin = request.getMethod() + " " + request.getRequestURI();
        ErrorLog.log(logger, origin, status, e);
        ErrorCode code = ErrorCode.of(e);
        code.count("rest", status);
        return Formats.negotiate(request).errorResponse(status, code, origin, ErrorCode.message(e));
    }
}
