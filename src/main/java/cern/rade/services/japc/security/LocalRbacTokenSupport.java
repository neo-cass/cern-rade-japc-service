package cern.rade.services.japc.security;

import cern.rade.services.japc.rbac.gateway.RbacGateway;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;
import cern.rbac.common.Role;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Same behaviour as rbac-util's {@code RbacAuthenticationService}, for the jakarta servlet API.
 */
public class LocalRbacTokenSupport implements RbacTokenSupport {
    private static final String TOKEN_PREFIX = "rbac-token";
    private final RbacGateway gateway;

    public LocalRbacTokenSupport(RbacGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public Optional<RbaToken> extract(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.trim().startsWith(TOKEN_PREFIX)) {
            return Optional.empty();
        }
        String encoded = header.trim().substring(TOKEN_PREFIX.length()).trim();
        try {
            return Optional.of(gateway.validate(Base64.getDecoder().decode(encoded)));
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("Invalid Base64 in Authorization rbac-token header", e);
        }
    }

    @Override
    public List<GrantedAuthority> authorities(RbaToken token) {
        Stream<String> location = token.getLocation() == null
                ? Stream.empty()
                : Stream.of(LOCATION_PREFIX + token.getLocation().getName());
        Stream<String> roles = Arrays.stream(token.getUser().getRoles()).map(Role::getName).map(RbacTokenSupport::roleAuthority);
        return Stream.concat(location, roles).<GrantedAuthority>map(SimpleGrantedAuthority::new).toList();
    }
}
