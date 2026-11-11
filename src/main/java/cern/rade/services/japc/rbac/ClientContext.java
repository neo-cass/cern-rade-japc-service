package cern.rade.services.japc.rbac;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

/**
 * Where a login came from, kept with the session so admins can tell clients apart: the client's address (the real
 * one behind the router, see {@code server.forward-headers-strategy}) and its User-Agent.
 */
public record ClientContext(String address, String userAgent) {
    public static final ClientContext UNKNOWN = new ClientContext(null, null);

    public static ClientContext of(HttpServletRequest request) {
        return new ClientContext(request.getRemoteAddr(), request.getHeader(HttpHeaders.USER_AGENT));
    }
}
