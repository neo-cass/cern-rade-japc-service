package cern.rade.services.japc.error;

import cern.rade.services.codec.CodecException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The mapping from exceptions to error code, HTTP status and client message.
 */
class ErrorCodeTest {
    /**
     * A LabVIEW body is decoded by the endpoint, so a CodecException reaches the error handler as is. It must give the
     * response the converter gave when it wrapped it in an HttpMessageNotReadableException.
     */
    @Test
    void codecErrorOfARequestBodyIsABadRequestWhetherOrNotItIsWrapped() {
        CodecException codec = new CodecException("bad data");
        HttpMessageNotReadableException wrapped = new HttpMessageNotReadableException(codec.getMessage(), codec, null);

        assertEquals(HttpStatus.BAD_REQUEST, ErrorCode.httpStatus(codec));
        assertEquals(ErrorCode.httpStatus(wrapped), ErrorCode.httpStatus(codec));
        assertEquals(ErrorCode.CODEC_ERROR, ErrorCode.of(codec));
        assertEquals(ErrorCode.of(wrapped), ErrorCode.of(codec));
        assertEquals(ErrorCode.message(wrapped), ErrorCode.message(codec));
    }

    @Test
    void internalErrorsHideTheirDetailFromTheClient() {
        IllegalStateException internal = new IllegalStateException("secret detail");

        assertEquals(ErrorCode.INTERNAL_ERROR, ErrorCode.of(internal));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.httpStatus(internal));
        assertEquals("Internal error", ErrorCode.message(internal));
    }

    @Test
    void clientErrorsKeepTheirMessage() {
        assertEquals("bad data", ErrorCode.message(new CodecException("bad data")));
        assertEquals("bad login", ErrorCode.message(new cern.rbac.client.authentication.AuthenticationException("bad login")));

        ResponseStatusException notFound = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field x not found");
        assertEquals("Field x not found", ErrorCode.message(notFound));
        assertEquals(ErrorCode.BAD_REQUEST, ErrorCode.of(notFound));
        assertEquals(HttpStatus.BAD_REQUEST, ErrorCode.httpStatus(notFound));
    }

    @Test
    void rateLimitedIs429() {
        cern.rade.services.japc.ratelimit.RateLimitedException limited = new cern.rade.services.japc.ratelimit.RateLimitedException("slow down");

        assertEquals(ErrorCode.RATE_LIMITED, ErrorCode.of(limited));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ErrorCode.httpStatus(limited));
    }
}
