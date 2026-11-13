package cern.rade.services.japc.set;

import cern.japc.value.ParameterValue;
import cern.japc.value.ValueDescriptor;
import cern.rade.services.codec.CodecException;

import java.util.function.Supplier;

/**
 * Provides the value of a set request, once the parameter it is set on is known.
 * <p>
 * Some formats cannot be decoded without the JAPC descriptor of the parameter (the enum type of an enum is not in the
 * request), so the value is produced on demand with that descriptor instead of being decoded up front.
 */
@FunctionalInterface
public interface ValueSource {
    /**
     * @param descriptor gives the JAPC descriptor of the parameter: the property descriptor for a property, the
     *                   descriptor of the field for a field. Evaluating it may query the device metadata, so only do
     *                   so if needed. It gives null if the descriptor is not available.
     * @return the value to set
     * @throws CodecException if the request data is not valid
     */
    ParameterValue valueFor(Supplier<ValueDescriptor> descriptor) throws CodecException;

    /**
     * @return a source of a value that was decoded without the descriptor, which is then never evaluated
     */
    static ValueSource of(ParameterValue value) {
        return descriptor -> value;
    }
}
