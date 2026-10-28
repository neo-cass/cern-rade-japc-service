package cern.rade.services.japc.format;

import cern.japc.value.ParameterValue;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.labview.LVDataDecoder;
import cern.rade.services.codec.labview.LVDataEncoder;
import cern.rade.services.codec.labview.LVTypeEncoder;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.nio.ByteBuffer;

public class LabVIEWConverter extends AbstractHttpMessageConverter<Object> {
    public LabVIEWConverter(MediaType mediaType) {
        super(mediaType);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return LabVIEWValue.class == clazz && canRead(mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return (NamedValue.class == clazz || LabVIEWValue.class == clazz) && canWrite(mediaType);
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException {
        String typeDescriptor = inputMessage.getHeaders().getFirst(LabVIEWFormat.TYPE_DESCRIPTOR_HEADER);
        if (typeDescriptor == null) {
            throw new HttpMessageNotReadableException(LabVIEWFormat.TYPE_DESCRIPTOR_HEADER + " header is required", inputMessage);
        }
        try {
            LVTypeDescriptor descriptor = LabVIEWFormat.typeDescriptorFromHeader(typeDescriptor);
            ParameterValue value = LVDataDecoder.decode(ByteBuffer.wrap(inputMessage.getBody().readAllBytes()), descriptor);
            return new LabVIEWValue(value, descriptor);
        } catch (CodecException e) {
            throw new HttpMessageNotReadableException(e.getMessage(), e, inputMessage);
        }
    }

    @Override
    protected void writeInternal(Object object, HttpOutputMessage outputMessage) throws IOException {
        try {
            byte[] body;
            if (object instanceof LabVIEWValue labviewValue) {
                body = LVDataEncoder.encode(labviewValue.value(), labviewValue.descriptor());
            } else {
                NamedValue namedValue = (NamedValue) object;
                LVTypeDescriptor descriptor = LVTypeEncoder.describe(namedValue.name(), namedValue.value());
                outputMessage.getHeaders().set(LabVIEWFormat.TYPE_DESCRIPTOR_HEADER,
                        LabVIEWFormat.typeDescriptorToHeader(LVTypeEncoder.encodeTypeDescriptor(descriptor, namedValue.value())));
                body = LVDataEncoder.encode(namedValue.value(), descriptor);
            }
            outputMessage.getBody().write(body);
        } catch (CodecException e) {
            throw new HttpMessageNotWritableException(e.getMessage(), e);
        }
    }
}
