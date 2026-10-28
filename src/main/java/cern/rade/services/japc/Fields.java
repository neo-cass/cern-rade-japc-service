package cern.rade.services.japc;

import cern.japc.core.factory.MapParameterValueFactory;
import cern.japc.value.MapParameterValue;
import cern.japc.value.ParameterValue;
import cern.japc.value.SimpleParameterValue;
import cern.japc.value.Type;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public final class Fields {
    private Fields() {
    }

    public static List<String> parse(String fields) {
        return fields == null ? null : List.of(fields.split(","));
    }

    public static ParameterValue select(ParameterValue value, List<String> fields) {
        if (fields == null || value == null) {
            return value;
        }
        if (value.getType() != Type.MAP) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fields can only be selected from a property, not a single field");
        }
        MapParameterValue map = value.castAsMap();
        MapParameterValue subset = MapParameterValueFactory.newMapParameterValue();
        for (String field : fields) {
            SimpleParameterValue fieldValue = map.get(field);
            if (fieldValue == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field " + field + " not found");
            }
            subset.put(field, fieldValue);
        }
        return subset;
    }
}
