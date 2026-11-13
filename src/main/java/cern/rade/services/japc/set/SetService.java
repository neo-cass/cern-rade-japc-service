package cern.rade.services.japc.set;

import cern.japc.core.Parameter;
import cern.japc.core.ParameterException;
import cern.japc.core.Selector;
import cern.japc.core.factory.SelectorFactory;
import cern.japc.value.ParameterValue;
import cern.japc.value.ValueDescriptor;
import cern.rade.services.codec.CodecException;
import cern.rade.services.japc.ParameterNames;
import cern.rade.services.japc.ParameterProvider;
import cern.rade.services.japc.rbac.RbacSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets one value on a JAPC parameter, once, on behalf of an RBAC session.
 */
public class SetService {
    private static final Logger logger = LoggerFactory.getLogger(SetService.class);

    private final ParameterProvider parameterProvider;
    private final String parameterName;
    private final String selectorName;
    private final ValueSource valueSource;
    private final RbacSession rbacSession;
    private boolean isExecuted = false;

    /**
     * Sets on a parameter created by the JAPC parameter factory.
     *
     * @param valueSource gives the value to set, from the descriptor of the parameter created by this service
     */
    public SetService(String selectorName, String device, String property, String field, ValueSource valueSource, RbacSession rbacSession) {
        this(ParameterProvider.japc(), selectorName, device, property, field, valueSource, rbacSession);
    }

    /**
     * @param parameterProvider creates the parameter to set on
     * @param valueSource      gives the value to set, from the descriptor of the parameter created by this service
     */
    public SetService(ParameterProvider parameterProvider, String selectorName, String device, String property, String field,
                      ValueSource valueSource, RbacSession rbacSession) {
        this.parameterProvider = parameterProvider;
        this.selectorName = ParameterNames.selector(selectorName);
        this.parameterName = ParameterNames.parameter(device, property, field);
        this.valueSource = valueSource;
        this.rbacSession = rbacSession;
    }

    /**
     * @return the JAPC descriptor of the parameter, or null if it cannot be obtained: a decoder only fails if it
     * actually needs it, with a more specific message than the reason why it is missing.
     */
    private ValueDescriptor valueDescriptor(Parameter parameter) {
        try {
            return parameter.getValueDescriptor();
        } catch (RuntimeException e) {
            logger.warn("No JAPC descriptor for {}: {}", parameterName, e.toString());
            return null;
        }
    }

    public void execute() throws ParameterException, CodecException {
        Parameter parameter = parameterProvider.newParameter(parameterName);
        Selector selector = SelectorFactory.newSelector(selectorName);
        ParameterValue parameterValue = valueSource.valueFor(() -> valueDescriptor(parameter));
        RbacSession.runAs(rbacSession, () -> parameter.setValue(selector, parameterValue));
        this.isExecuted = true;
    }

    public boolean isExecuted() {
        return isExecuted;
    }
}
