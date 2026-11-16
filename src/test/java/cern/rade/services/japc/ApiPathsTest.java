package cern.rade.services.japc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** How WebSocket paths are mapped to the device and property, with and without a version. */
class ApiPathsTest {
    @Test
    void versionedAndUnversionedPrefixesAreStripped() {
        assertEquals("/DEV/Prop", ApiPaths.stripWebSocketBase("/ws/v1/DEV/Prop"));
        assertEquals("/DEV/Prop", ApiPaths.stripWebSocketBase("/ws/DEV/Prop"));
        assertEquals("/DEV", ApiPaths.stripWebSocketBase("/ws/DEV"));
    }

    @Test
    void versionSegmentsAreNeverDevices() {
        assertNull(ApiPaths.stripWebSocketBase("/ws/v2/DEV/Prop"));
        assertNull(ApiPaths.stripWebSocketBase("/ws/v10/DEV"));
        assertNull(ApiPaths.stripWebSocketBase("/ws/v1/v1/Prop"));
    }

    @Test
    void otherPrefixesAreNotRecognized() {
        assertNull(ApiPaths.stripWebSocketBase("/other/DEV/Prop"));
    }

    @Test
    void devicesThatMerelyStartWithVAreKept() {
        assertEquals("/vacuum/Prop", ApiPaths.stripWebSocketBase("/ws/v1/vacuum/Prop"));
        assertEquals("/v2x/Prop", ApiPaths.stripWebSocketBase("/ws/v2x/Prop"));
    }
}
