package cern.rade.services.japc.parameter;

import cern.japc.core.Parameters;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Where a JAPC call goes: device, property, optional field, and the cycle selector.
 */
public record ParameterAddress(String device, String property, String field, String selector) {
    public ParameterAddress {
        requireName(device, "device");
        requireName(property, "property");
        if (field != null) {
            requireName(field, "field");
        }
        if (selector == null) {
            selector = "";
        } else if (hasControlChar(selector)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid selector");
        }
    }

    /**
     * @return the full JAPC parameter name
     */
    public String parameterName() {
        return field == null ? Parameters.buildParameterName(device, property) : Parameters.buildParameterName(device, property, field);
    }

    // '/' and '#' separate the parts of a JAPC parameter name, inside a part they would address another parameter.
    private static void requireName(String name, String part) {
        if (name == null || name.isBlank() || name.indexOf('/') >= 0 || name.indexOf('#') >= 0 || hasControlChar(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + part + " name");
        }
    }

    private static boolean hasControlChar(String text) {
        return text.chars().anyMatch(Character::isISOControl);
    }
}
