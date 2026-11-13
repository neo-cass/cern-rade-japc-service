package cern.rade.services.japc.format;

import cern.japc.value.ParameterValue;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;

/**
 * A value to send to a LabVIEW client, encoded as described by the type descriptor the client asked for.
 */
public record LabVIEWValue(ParameterValue value, LVTypeDescriptor descriptor) {
}
