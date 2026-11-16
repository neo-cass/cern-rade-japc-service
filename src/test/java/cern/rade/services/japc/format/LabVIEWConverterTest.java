package cern.rade.services.japc.format;

import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.labview.LVTypeEncoder;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reading LabVIEW request bodies: what is accepted, deferred to the endpoint and refused.
 */
class LabVIEWConverterTest {
    private final LabVIEWConverter converter = new LabVIEWConverter(new LabVIEWFormat().mediaType(), 64);

    private static HttpInputMessage request(String typeDescriptorHeader, byte[] body) {
        HttpHeaders headers = new HttpHeaders();
        if (typeDescriptorHeader != null) {
            headers.set(LabVIEWFormat.TYPE_DESCRIPTOR_HEADER, typeDescriptorHeader);
        }
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(body);
            }

            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        };
    }

    private static String settingHeader() throws CodecException {
        LVTypeDescriptor count = new LVTypeDescriptor(LVType.INT_32, "count");
        return LabVIEWFormat.typeDescriptorToHeader(
                LVTypeEncoder.encodeTypeDescriptor(new LVClusterDescriptor("Setting", new LVTypeDescriptor[]{count})));
    }

    @Test
    void readsLabVIEWBodiesOnly() {
        assertTrue(converter.canRead(LabVIEWBody.class, new LabVIEWFormat().mediaType()));
        assertFalse(converter.canRead(LabVIEWValue.class, new LabVIEWFormat().mediaType()));
    }

    @Test
    void readDefersDecodingToTheEndpoint() throws Exception {
        // the data is kept as is, to be decoded by the endpoint once the JAPC descriptor of the parameter is known
        LabVIEWBody body = assertInstanceOf(LabVIEWBody.class, converter.read(LabVIEWBody.class, request(settingHeader(), new byte[]{0, 0, 0, 42})));

        assertEquals("Setting", body.name());
        assertEquals(42, body.matching("Setting", null).decode(null).castAsMap().get("count").getInt());
    }

    @Test
    void readDoesNotRejectBadDataBeforeTheEndpointDecodesIt() throws Exception {
        LabVIEWBody body = (LabVIEWBody) converter.read(LabVIEWBody.class, request(settingHeader(), new byte[0]));
        assertThrows(CodecException.class, () -> body.decode(null));
    }

    @Test
    void missingTypeDescriptorIsNotReadable() {
        assertThrows(HttpMessageNotReadableException.class, () -> converter.read(LabVIEWBody.class, request(null, new byte[0])));
    }

    @Test
    void invalidTypeDescriptorIsNotReadable() {
        for (String header : new String[]{"not base64 !", "AAAA"}) {
            assertThrows(HttpMessageNotReadableException.class, () -> converter.read(LabVIEWBody.class, request(header, new byte[0])), header);
        }
    }

    @Test
    void typeDescriptorWithNegativeCountIsNotReadable() {
        // a cluster whose element count is negative: the codec fails with an unchecked exception
        String header = Base64.getUrlEncoder().encodeToString(new byte[]{0, 6, 0, 0x50, (byte) 0xFF, (byte) 0xFF});

        assertThrows(HttpMessageNotReadableException.class, () -> converter.read(LabVIEWBody.class, request(header, new byte[0])));
    }

    @Test
    void bodyLargerThanTheCapIsRefused() throws Exception {
        HttpInputMessage message = request(settingHeader(), new byte[65]);

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> converter.read(LabVIEWBody.class, message));
        assertEquals(413, e.getStatusCode().value());
    }

    @Test
    void bodyOfExactlyTheCapIsRead() throws Exception {
        LabVIEWBody body = (LabVIEWBody) converter.read(LabVIEWBody.class, request(settingHeader(), new byte[64]));
        assertEquals("Setting", body.name());
    }
}
