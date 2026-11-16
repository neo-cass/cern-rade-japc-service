package cern.rade.services.japc.format;

import cern.japc.value.ParameterValue;

/**
 * A value to send together with the name of its property or field, which the LabVIEW format needs to label the type
 * descriptor it derives.
 */
public record NamedValue(String name, ParameterValue value) {
}
