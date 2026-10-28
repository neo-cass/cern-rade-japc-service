package cern.rade.services.japc;

import cern.japc.core.Parameters;

public final class ParameterNames {
    private ParameterNames() {
    }

    public static String parameter(String device, String property, String field) {
        return field == null ? Parameters.buildParameterName(device, property) : Parameters.buildParameterName(device, property, field);
    }

    public static String selector(String selector) {
        return selector == null ? "" : selector;
    }
}
