package cern.rade.services.japc.format;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Which format a request or a WebSocket handshake gets, from its Accept, Content-Type and subprotocol headers.
 */
class FormatsTest {
    private static HttpServletRequest request(String accept, String contentType) {
        return (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getHeader" -> HttpHeaders.ACCEPT.equalsIgnoreCase((String) args[0]) ? accept : null;
                    case "getContentType" -> contentType;
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }

    private static HttpHeaders protocols(String value) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Sec-WebSocket-Protocol", value);
        return headers;
    }

    @Test
    void noAcceptGetsJson() {
        assertInstanceOf(JSONFormat.class, Formats.negotiate(request(null, null)));
    }

    @Test
    void acceptLabViewGetsLabView() {
        assertInstanceOf(LabVIEWFormat.class, Formats.negotiate(request(LabVIEWFormat.MEDIA_TYPE, null)));
    }

    @Test
    void labViewRequestBodyWithAcceptAnythingGetsLabView() {
        assertInstanceOf(LabVIEWFormat.class, Formats.negotiate(request("*/*", LabVIEWFormat.MEDIA_TYPE)));
    }

    @Test
    void unparsableAcceptGetsJson() {
        assertInstanceOf(JSONFormat.class, Formats.negotiate(request("not a media type", null)));
    }

    @Test
    void qualityValuesDecide() {
        assertInstanceOf(JSONFormat.class,
                Formats.negotiate(request(LabVIEWFormat.MEDIA_TYPE + ";q=0.1, application/json", null)));
    }

    @Test
    void handshakeWithoutSubprotocolGetsJson() {
        assertInstanceOf(JSONFormat.class, Formats.forHandshake(new HttpHeaders()));
    }

    @Test
    void handshakeSubprotocolPicksTheFormat() {
        assertInstanceOf(LabVIEWFormat.class, Formats.forHandshake(protocols("labview-variant")));
        assertEquals("json", Formats.forHandshake(protocols("json")).subprotocol());
    }

    @Test
    void handshakeWithUnknownSubprotocolGetsNoFormat() {
        assertNull(Formats.forHandshake(protocols("xml")));
    }
}
