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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LabVIEWConverterTest {
    private final LabVIEWConverter converter = new LabVIEWConverter(new LabVIEWFormat().mediaType());

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
}
