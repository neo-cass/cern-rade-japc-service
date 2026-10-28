package cern.rade.services.japc.error;

import cern.cmw.rda3.common.exception.InvalidArgumentException;
import cern.cmw.rda3.common.exception.InvalidRequestException;
import cern.cmw.rda3.common.exception.NameServiceException;
import cern.cmw.rda3.common.exception.SecurityException;
import cern.cmw.rda3.common.exception.TimeoutException;
import cern.japc.core.AuthorizationExpiredException;
import cern.japc.core.ParameterException;
import cern.japc.core.transaction.BadSettingValueException;
import cern.rade.services.codec.CodecException;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.socket.CloseStatus;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public enum ErrorCode {
    JAPC_ERROR(5000),
    CODEC_ERROR(5001),
    RBAC_ERROR(5002),
    BAD_REQUEST(5003),
    INTERNAL_ERROR(5004);

    private static final int MAX_CLOSE_REASON_BYTES = 123;
    private final int value;

    ErrorCode(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public CloseStatus closeStatus(String message) {
        String reason = message;
        while (reason.getBytes(StandardCharsets.UTF_8).length > MAX_CLOSE_REASON_BYTES) {
            reason = reason.substring(0, reason.length() - 1);
        }
        return new CloseStatus(4000 + (value - JAPC_ERROR.value), reason);
    }

    public static ErrorCode of(Throwable e) {
        if (e instanceof HttpMessageConversionException && e.getCause() != null) {
            return of(e.getCause());
        }
        if (e instanceof HttpMessageNotReadableException) {
            return BAD_REQUEST;
        }
        if (e instanceof TokenFormatException || e instanceof AuthenticationException
                || causedBy(e, AuthorizationExpiredException.class) || causedBy(e, SecurityException.class)) {
            return RBAC_ERROR;
        }
        if (e instanceof ParameterException) {
            return JAPC_ERROR;
        }
        if (e instanceof CodecException) {
            return CODEC_ERROR;
        }
        if (e instanceof ErrorResponse errorResponse && errorResponse.getStatusCode().is4xxClientError()) {
            return BAD_REQUEST;
        }
        return INTERNAL_ERROR;
    }

    public static HttpStatusCode httpStatus(Throwable e) {
        if (e instanceof HttpMessageNotReadableException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (e instanceof HttpMessageNotWritableException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (e instanceof ErrorResponse errorResponse) {
            return errorResponse.getStatusCode();
        }
        if (e instanceof TokenFormatException || e instanceof AuthenticationException || causedBy(e, AuthorizationExpiredException.class)) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (causedBy(e, SecurityException.class)) {
            return HttpStatus.FORBIDDEN;
        }
        if (causedBy(e, NameServiceException.class)) {
            return HttpStatus.NOT_FOUND;
        }
        if (causedBy(e, InvalidArgumentException.class) || causedBy(e, InvalidRequestException.class) || causedBy(e, BadSettingValueException.class)) {
            return HttpStatus.BAD_REQUEST;
        }
        if (causedBy(e, TimeoutException.class)) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }
        if (e instanceof ParameterException) {
            return HttpStatus.BAD_GATEWAY;
        }
        if (e instanceof CodecException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static boolean causedBy(Throwable e, Class<? extends Throwable> type) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (type.isInstance(cause)) {
                return true;
            }
        }
        return false;
    }

    public static String message(Throwable e) {
        if (e instanceof ErrorResponse errorResponse && errorResponse.getBody().getDetail() != null) {
            return errorResponse.getBody().getDetail();
        }
        return Objects.toString(e.getMessage(), e.getClass().getSimpleName());
    }
}
