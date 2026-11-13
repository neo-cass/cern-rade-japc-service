package cern.rade.services.japc.error;

import cern.rade.services.codec.CodecException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
