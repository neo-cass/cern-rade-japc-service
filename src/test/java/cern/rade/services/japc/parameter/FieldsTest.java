package cern.rade.services.japc.parameter;

import cern.japc.core.factory.MapParameterValueFactory;
import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.value.MapParameterValue;
import cern.japc.value.ParameterValue;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The {@code fields} parameter: parsing the list and selecting fields of a property value.
 */
class FieldsTest {
    private static MapParameterValue map() {
        MapParameterValue map = MapParameterValueFactory.newMapParameterValue();
        map.put("a", SimpleParameterValueFactory.newSimpleParameterValue(1));
        map.put("b", SimpleParameterValueFactory.newSimpleParameterValue(2));
        map.put("c", SimpleParameterValueFactory.newSimpleParameterValue(3));
        return map;
    }

    private static void assertBadRequest(Runnable action) {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class, action::run).getStatusCode());
    }

    @Test
    void parseKeepsNullAndSplitsOnCommas() {
        assertNull(Fields.parse(null));
        assertEquals(List.of("a", "b"), Fields.parse("a,b"));
    }

    @Test
    void selectReturnsOnlyTheRequestedFields() {
        ParameterValue selected = Fields.select(map(), List.of("a", "c"));
        MapParameterValue result = selected.castAsMap();
        assertEquals(2, result.size());
        assertEquals(1, result.getInt("a"));
        assertEquals(3, result.getInt("c"));
    }

    @Test
    void unknownFieldIsABadRequest() {
        assertBadRequest(() -> Fields.select(map(), List.of("nope")));
    }

    @Test
    void fieldsCannotBeSelectedFromASimpleValue() {
        assertBadRequest(() -> Fields.select(SimpleParameterValueFactory.newSimpleParameterValue(1), List.of("a")));
    }

    @Test
    void withoutFieldsTheValueIsUnchanged() {
        MapParameterValue map = map();
        assertSame(map, Fields.select(map, null));
    }
}
