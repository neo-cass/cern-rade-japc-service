package cern.rade.services.japc.rbac.login;

import cern.rbac.client.authentication.AuthenticationException;

import java.util.Base64;

/**
 * The value of an {@code Rbac: <scheme> <value>} header.
 */
public record RbacCredentials(String scheme, String value) {

    public static RbacCredentials parse(String header) {
        String[] parts = header.trim().split("\\s+", 2);
        if (parts.length != 2) {
            throw new AuthenticationException("Invalid authentication format. Expected '<scheme> <value>'");
        }
        return new RbacCredentials(parts[0], parts[1]);
    }

    /**
     * Decodes the value as URL-safe Base64, the encoding all binary schemes use.
     */
    public byte[] decodedValue() {
        try {
            return Base64.getUrlDecoder().decode(value);
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("Invalid Base64 in Rbac " + scheme + " header", e);
        }
    }

    @Override
    public String toString() {
        return "RbacCredentials[scheme=" + scheme + "]";
    }
}
