package cern.rade.services.japc.format;

import cern.japc.value.ParameterValue;

public record NamedValue(String name, ParameterValue value) {
}
