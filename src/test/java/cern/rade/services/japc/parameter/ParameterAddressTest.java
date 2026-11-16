package cern.rade.services.japc.parameter;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Parameter names and the validation of the parts of a parameter address.
 */
class ParameterAddressTest {
    private static final List<String> BAD_NAMES = List.of("A/B", "A#B", "A\nB", "", "  ");

    private static void assertBadRequest(String message, Runnable create) {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, create::run);
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        assertEquals(message, e.getReason());
    }

    @Test
    void parameterNameOfAProperty() {
        assertEquals("DEVICE/Property", new ParameterAddress("DEVICE", "Property", null, null).parameterName());
    }

    @Test
    void parameterNameOfAField() {
        assertEquals("DEVICE/Property#field", new ParameterAddress("DEVICE", "Property", "field", null).parameterName());
    }

    @Test
    void missingSelectorBecomesEmpty() {
        assertEquals("", new ParameterAddress("DEVICE", "Property", null, null).selector());
    }

    @Test
    void invalidPartsAreBadRequests() {
        for (String bad : BAD_NAMES) {
            assertBadRequest("Invalid device name", () -> new ParameterAddress(bad, "P", null, null));
            assertBadRequest("Invalid property name", () -> new ParameterAddress("D", bad, null, null));
            assertBadRequest("Invalid field name", () -> new ParameterAddress("D", "P", bad, null));
        }
    }

    @Test
    void deviceAndPropertyAreRequired() {
        assertBadRequest("Invalid device name", () -> new ParameterAddress(null, "P", null, null));
        assertBadRequest("Invalid property name", () -> new ParameterAddress("D", null, null, null));
    }

    @Test
    void selectorWithControlCharacterIsRejected() {
        assertBadRequest("Invalid selector", () -> new ParameterAddress("D", "P", null, "LHC.USER\nALL"));
    }

    @Test
    void ordinaryNamesAreAccepted() {
        ParameterAddress address = new ParameterAddress("RADE.TEST-1_a", "Prop_1", "field-2", "LHC.USER.ALL");
        assertEquals("RADE.TEST-1_a/Prop_1#field-2", address.parameterName());
        assertEquals("LHC.USER.ALL", address.selector());
    }
}
