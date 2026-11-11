package cern.rade.services.japc.rbac.login;

import cern.rade.services.japc.rbac.ClientContext;
import cern.rbac.client.authentication.AuthenticationException;

import java.util.Base64;

/**
 * The value of an {@code Rbac: <scheme> <value>} header, and the client that sent it.
 */
public record RbacCredentials(String scheme, String value, ClientContext client) {

    public RbacCredentials(String scheme, String value) {
        this(scheme, value, ClientContext.UNKNOWN);
    }

    public static RbacCredentials parse(String header) {
        return parse(header, ClientContext.UNKNOWN);
    }

    public static RbacCredentials parse(String header, ClientContext client) {
        String[] parts = header.trim().split("\\s+", 2);
        if (parts.length != 2) {
            throw new AuthenticationException("Invalid authentication format. Expected '<scheme> <value>'");
        }
        return new RbacCredentials(parts[0], parts[1], client);
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
