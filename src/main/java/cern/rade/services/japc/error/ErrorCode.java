package cern.rade.services.japc.error;

import cern.japc.core.ParameterException;
import cern.rade.services.codec.CodecException;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.ErrorResponse;
import org.springframework.web.socket.CloseStatus;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class ErrorCode {
    public static final int JAPC_ERROR = 5000;
    public static final int CODEC_ERROR = 5001;
    public static final int RBAC_ERROR = 5002;
    public static final int BAD_REQUEST = 5003;
    public static final int INTERNAL_ERROR = 5004;
    private static final int MAX_CLOSE_REASON_BYTES = 123;

    private ErrorCode() {
    }

    public static int code(Throwable e) {
        if (e instanceof ParameterException) {
            return JAPC_ERROR;
        }
        if (e instanceof CodecException) {
            return CODEC_ERROR;
        }
        if (e instanceof TokenFormatException || e instanceof AuthenticationException) {
            return RBAC_ERROR;
        }
        if (e instanceof ErrorResponse errorResponse && errorResponse.getStatusCode().is4xxClientError()) {
            return BAD_REQUEST;
        }
        return INTERNAL_ERROR;
    }

    public static HttpStatusCode httpStatus(Throwable e) {
        if (e instanceof ErrorResponse errorResponse) {
            return errorResponse.getStatusCode();
        }
        return httpStatus(code(e));
    }

    public static HttpStatusCode httpStatus(int code) {
        return code == INTERNAL_ERROR ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
    }

    public static String message(Throwable e) {
        return Objects.toString(e.getMessage(), e.getClass().getSimpleName());
    }

    public static CloseStatus closeStatus(int code, String message) {
        String reason = message;
        while (reason.getBytes(StandardCharsets.UTF_8).length > MAX_CLOSE_REASON_BYTES) {
            reason = reason.substring(0, reason.length() - 1);
        }
        return new CloseStatus(4000 + (code - JAPC_ERROR), reason);
    }
}
