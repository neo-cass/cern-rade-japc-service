package cern.rade.services.japc.format;

import cern.japc.value.MapParameterValue;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * What LabVIEWBody adds to the codec: matching the client's type descriptor to the request, and keeping the data
 * until it is decoded. How the data is decoded is tested by the codec.
 */
class LabVIEWBodyTest {
    private static final LVTypeDescriptor LV_LIMIT = new LVTypeDescriptor(LVType.INT_32, "limit");
    private static final LVTypeDescriptor LV_COUNT = new LVTypeDescriptor(LVType.INT_32, "count");

    /** Property Setting {limit, count}, with limit = 7 and count = 42. */
    private static final byte[] SETTING_DATA = {0, 0, 0, 7, 0, 0, 0, 42};
    private static final byte[] LIMIT_7 = {0, 0, 0, 7};

    private static LabVIEWBody setting() {
        return new LabVIEWBody(new LVClusterDescriptor("Setting", new LVTypeDescriptor[]{LV_LIMIT, LV_COUNT}), SETTING_DATA);
    }

    private static LabVIEWBody singleFieldSetting() {
        return new LabVIEWBody(new LVClusterDescriptor("Setting", new LVTypeDescriptor[]{LV_LIMIT}), LIMIT_7);
    }

    @Test
    void bodyIsNotAffectedByLaterChangesToTheArray() throws CodecException {
        byte[] data = {0, 0, 0, 42};
        LabVIEWBody body = new LabVIEWBody(new LVClusterDescriptor("Setting", new LVTypeDescriptor[]{LV_COUNT}), data);

        data[3] = 1;

        assertEquals(42, body.matching("Setting", null).decode(null).castAsMap().get("count").getInt());
    }

    @Test
    void propertyWithoutEnumNeedsNoDescriptor() throws CodecException {
        MapParameterValue value = setting().matching("Setting", null).decode(null).castAsMap();
        assertEquals(7, value.get("limit").getInt());
        assertEquals(42, value.get("count").getInt());
    }

    @Test
    void singleFieldRequestIsDecodedOverTheSameBytesAsTheOneFieldCluster() throws CodecException {
        LabVIEWBody body = singleFieldSetting();

        assertEquals(7, body.matching("Setting", null).decode(null).castAsMap().get("limit").getInt());
        assertEquals(7, body.matching("Setting", "limit").decode(null).castAsSimple().getInt());
    }

    @Test
    void fieldRequestSentAsTheFieldTypeDescriptorIsDecodedAsTheField() throws CodecException {
        LabVIEWBody body = new LabVIEWBody(LV_LIMIT, LIMIT_7);
        assertEquals(7, body.matching("Setting", "limit").decode(null).castAsSimple().getInt());
    }

    @Test
    void theDescriptorMatchingTheRequestIsKept() {
        LabVIEWBody body = setting();
        assertSame(body, body.matching("Setting", null));
        assertEquals("Setting", body.matching(null, null).name());
        assertEquals("limit", singleFieldSetting().matching("Setting", "limit").name());
    }

    @Test
    void typeDescriptorNotMatchingTheRequestIsABadRequest() {
        for (LabVIEWBody body : new LabVIEWBody[]{setting(), singleFieldSetting()}) {
            // wrong property, wrong field, and a field of a cluster that has several
            for (String[] request : new String[][]{{"Other", null}, {"Setting", "other"}, {"Other", "limit"}}) {
                ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> body.matching(request[0], request[1]));
                assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            }
        }
        assertThrows(ResponseStatusException.class, () -> setting().matching("Setting", "limit"));
    }

    @Test
    void canBeDecodedMoreThanOnce() throws CodecException {
        LabVIEWBody body = setting().matching("Setting", null);
        assertEquals(42, body.decode(null).castAsMap().get("count").getInt());
        assertEquals(42, body.decode(null).castAsMap().get("count").getInt());
    }
}
