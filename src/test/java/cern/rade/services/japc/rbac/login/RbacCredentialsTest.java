package cern.rade.services.japc.rbac.login;

import cern.rbac.client.authentication.AuthenticationException;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Parsing of the {@code Rbac} header value.
 */
class RbacCredentialsTest {

    @Test
    void parsesSchemeAndValue() {
        RbacCredentials credentials = RbacCredentials.parse("  Cached   1234-abcd ");
        assertEquals("Cached", credentials.scheme());
        assertEquals("1234-abcd", credentials.value());
    }

    @Test
    void rejectsHeaderWithoutValue() {
        assertThrows(AuthenticationException.class, () -> RbacCredentials.parse("Explicit"));
    }

    @Test
    void decodesUrlSafeBase64() {
        byte[] bytes = "user:pa?ss>".getBytes(StandardCharsets.ISO_8859_1);
        RbacCredentials credentials = new RbacCredentials("Explicit", Base64.getUrlEncoder().encodeToString(bytes));
        assertArrayEquals(bytes, credentials.decodedValue());
    }

    @Test
    void decodedTextIsUtf8() {
        assertEquals("user:p\u00e4ssw\u00f6rd\u20ac", textOf("user:p\u00e4ssw\u00f6rd\u20ac", StandardCharsets.UTF_8));
    }

    @Test
    void decodedTextFallsBackToIso88591() {
        assertEquals("user:p\u00e4ssw\u00f6rd", textOf("user:p\u00e4ssw\u00f6rd", StandardCharsets.ISO_8859_1));
    }

    @Test
    void decodedTextLeavesAsciiUnchanged() {
        assertEquals("user:password", textOf("user:password", StandardCharsets.UTF_8));
    }

    @Test
    void decodedTextRejectsInvalidBase64() {
        assertThrows(AuthenticationException.class, () -> new RbacCredentials("Explicit", "not base64!").decodedText());
    }

    private static String textOf(String text, Charset charset) {
        return new RbacCredentials("Explicit", Base64.getUrlEncoder().encodeToString(text.getBytes(charset))).decodedText();
    }

    @Test
    void rejectsInvalidBase64() {
        assertThrows(AuthenticationException.class, () -> new RbacCredentials("Kerberos", "not base64!").decodedValue());
    }

    @Test
    void toStringHidesTheValue() {
        assertFalse(new RbacCredentials("Explicit", "secret").toString().contains("secret"));
    }
}
