package cern.rade.services.japc.parameter;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.Parameter;
import cern.japc.core.ParameterException;
import cern.japc.core.Selector;
import cern.japc.core.factory.SelectorFactory;
import cern.japc.value.ParameterValue;
import cern.japc.value.ValueDescriptor;
import cern.rade.services.codec.CodecException;
import cern.rade.services.japc.rbac.RbacSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Reads and writes JAPC parameters with the RBAC token of the calling session.
 */
@Service
public class ParameterService {
    private static final Logger logger = LoggerFactory.getLogger(ParameterService.class);

    private final ParameterProvider parameters;

    public ParameterService(ParameterProvider parameters) {
        this.parameters = parameters;
    }

    public AcquiredParameterValue get(ParameterAddress address, RbacSession session) throws ParameterException {
        Parameter parameter = parameters.newParameter(address.parameterName());
        Selector selector = SelectorFactory.newSelector(address.selector());
        return RbacSession.callAs(session, () -> parameter.getValue(selector));
    }

    /**
     * @param source gives the value to set, from the descriptor of the parameter created by this call
     */
    public void set(ParameterAddress address, ValueSource source, RbacSession session) throws ParameterException, CodecException {
        Parameter parameter = parameters.newParameter(address.parameterName());
        Selector selector = SelectorFactory.newSelector(address.selector());
        ParameterValue parameterValue = source.valueFor(() -> valueDescriptor(address, parameter));
        RbacSession.runAs(session, () -> parameter.setValue(selector, parameterValue));
    }

    /**
     * @return the JAPC descriptor of the parameter, or null if it cannot be obtained: a decoder only fails if it
     * actually needs it, with a more specific message than the reason why it is missing.
     */
    private ValueDescriptor valueDescriptor(ParameterAddress address, Parameter parameter) {
        try {
            return parameter.getValueDescriptor();
        } catch (RuntimeException e) {
            logger.warn("No JAPC descriptor for {}: {}", address.parameterName(), e.toString());
            return null;
        }
    }
}
