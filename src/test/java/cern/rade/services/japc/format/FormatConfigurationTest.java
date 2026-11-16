package cern.rade.services.japc.format;

import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The accepted range of the request body limit.
 */
class FormatConfigurationTest {
    @Test
    void refusesLimitsThatAreNotPositiveOrTooLargeForOneArray() {
        for (DataSize size : new DataSize[]{DataSize.ofBytes(0), DataSize.ofBytes(-1), DataSize.ofGigabytes(2)}) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new FormatConfiguration(size), size.toString());
            assertTrue(e.getMessage().contains("max-request-body"), e.getMessage());
        }
    }

    @Test
    void acceptsTheDefault() {
        assertDoesNotThrow(() -> new FormatConfiguration(DataSize.ofMegabytes(16)));
    }
}
