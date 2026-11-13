package cern.rade.services.japc.set;

import cern.japc.value.ParameterValue;
import cern.rade.services.japc.security.RbacAuthentication;

public abstract class SetEndpoint {

    protected SetService setService(String selector, String device, String property, String field, ParameterValue data) {
        return setService(selector, device, property, field, ValueSource.of(data));
    }

    protected SetService setService(String selector, String device, String property, String field, ValueSource data) {
        return new SetService(selector, device, property, field, data, RbacAuthentication.current().orElse(null));
    }
}
