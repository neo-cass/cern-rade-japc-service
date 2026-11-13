package cern.rade.services.japc.format;

import cern.japc.value.ParameterValue;
import cern.japc.value.ValueDescriptor;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.labview.LVDataDecoder;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;

import java.nio.ByteBuffer;

/**
 * The body of a LabVIEW request, kept flattened until it can be decoded.
 * <p>
 * LabVIEW enums and enum sets only carry the names of their items, so decoding them needs the JAPC enum type of the
 * parameter, which is only known once the endpoint has resolved the device and property. The converter therefore
 * only reads the type descriptor and the bytes, and the endpoint decodes them with {@link #decode(ValueDescriptor)}.
 * <p>
 * Instances are immutable and can be decoded more than once.
 */
public final class LabVIEWBody {
    private final LVTypeDescriptor descriptor;
    private final byte[] data;

    /**
     * @param descriptor the type descriptor sent by the client
     * @param data       the flattened data, as sent by the client; copied, so later changes to the array do not
     *                   affect this body
     */
    public LabVIEWBody(LVTypeDescriptor descriptor, byte[] data) {
        this.descriptor = descriptor;
        this.data = data.clone();
    }

    /**
     * Checks that the client's type descriptor describes the requested property or field, see
     * {@link LabVIEWFormat#matchDescriptor}. For a field, the field's own type descriptor is kept: a cluster flattens
     * as the concatenation of its fields, so a one-field cluster and that field share the same bytes.
     *
     * @param property the requested property, or null if the request only names the device
     * @param field    the requested field, or null if the request is for the whole property
     * @return a body to decode with the JAPC descriptor of the requested property or field
     * @throws org.springframework.web.server.ResponseStatusException (400) if the type descriptor does not match
     */
    public LabVIEWBody matching(String property, String field) {
        LVTypeDescriptor matched = LabVIEWFormat.matchDescriptor(descriptor, property, field);
        return matched == descriptor ? this : new LabVIEWBody(matched, data);
    }

    /**
     * @return the name of the type descriptor, which names the property of a request that only names the device
     */
    public String name() {
        return descriptor.getName();
    }

    /**
     * Decodes the data.
     *
     * @param valueDescriptor the JAPC descriptor of the parameter: its property descriptor if the type descriptor
     *                        is a cluster, otherwise the descriptor of the field. Only consulted for enums and enum
     *                        sets, may be null if the data has none.
     * @return a map value for a cluster, otherwise a simple value
     * @throws CodecException if the data does not match the type descriptor, or an enum needs a JAPC enum type that
     *                        {@code valueDescriptor} does not provide
     */
    public ParameterValue decode(ValueDescriptor valueDescriptor) throws CodecException {
        return LVDataDecoder.decode(ByteBuffer.wrap(data), descriptor, valueDescriptor);
    }
}
