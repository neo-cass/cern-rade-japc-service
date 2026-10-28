package cern.rade.services.japc.format;

import cern.japc.value.ParameterValue;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.json.JSONCodec;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JSONConverter extends AbstractHttpMessageConverter<Object> {
    public JSONConverter(MediaType mediaType) {
        super(StandardCharsets.UTF_8, mediaType);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return ParameterValue.class == clazz && canRead(mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return NamedValue.class == clazz && canWrite(mediaType);
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException {
        try {
            return JSONCodec.decode(new String(inputMessage.getBody().readAllBytes(), StandardCharsets.UTF_8));
        } catch (CodecException e) {
            throw new HttpMessageNotReadableException(e.getMessage(), e, inputMessage);
        }
    }

    @Override
    protected void writeInternal(Object object, HttpOutputMessage outputMessage) throws IOException {
        try {
            outputMessage.getBody().write(JSONCodec.encode(((NamedValue) object).value()).getBytes(StandardCharsets.UTF_8));
        } catch (CodecException e) {
            throw new HttpMessageNotWritableException(e.getMessage(), e);
        }
    }
}
