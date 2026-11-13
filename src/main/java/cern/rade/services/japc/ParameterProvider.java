package cern.rade.services.japc;

import cern.japc.core.Parameter;
import cern.japc.core.ParameterException;
import cern.japc.core.factory.ParameterFactory;

/**
 * Creates the JAPC parameters the services work on, so that they can be given another source than the JAPC factory.
 */
@FunctionalInterface
public interface ParameterProvider {
    /**
     * @param parameterName the full name of the parameter, see {@link ParameterNames#parameter}
     * @return the parameter
     * @throws ParameterException if the parameter cannot be created
     */
    Parameter newParameter(String parameterName) throws ParameterException;

    /**
     * @return a provider that creates the parameters with the JAPC parameter factory
     */
    static ParameterProvider japc() {
        return name -> ParameterFactory.newInstance().newParameter(name);
    }
}
