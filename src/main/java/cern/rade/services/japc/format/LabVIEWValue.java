package cern.rade.services.japc.format;

import cern.japc.value.ParameterValue;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;

public record LabVIEWValue(ParameterValue value, LVTypeDescriptor descriptor) {
}
