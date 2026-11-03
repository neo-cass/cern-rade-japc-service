package cern.rade.services.japc.rbac.login;

import cern.rbac.client.authentication.AuthenticationException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void rejectsInvalidBase64() {
        assertThrows(AuthenticationException.class, () -> new RbacCredentials("Kerberos", "not base64!").decodedValue());
    }

    @Test
    void toStringHidesTheValue() {
        assertFalse(new RbacCredentials("Explicit", "secret").toString().contains("secret"));
    }
}
