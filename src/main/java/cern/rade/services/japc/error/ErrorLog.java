package cern.rade.services.japc.error;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public final class ErrorLog {
    private ErrorLog() {
    }

    public static void log(Logger logger, String context, HttpStatusCode status, String message, Throwable cause) {
        if (status.isSameCodeAs(HttpStatus.INTERNAL_SERVER_ERROR)) {
            logger.error("{} failed with {}: {}", context, status.value(), message, cause);
        } else {
            logger.warn("{} failed with {}: {}", context, status.value(), message);
        }
    }

    public static void log(Logger logger, String context, HttpStatusCode status, Throwable e) {
        log(logger, context, status, ErrorCode.message(e), e);
    }

    public static void log(Logger logger, String context, Throwable e) {
        log(logger, context, ErrorCode.httpStatus(e), e);
    }
}
