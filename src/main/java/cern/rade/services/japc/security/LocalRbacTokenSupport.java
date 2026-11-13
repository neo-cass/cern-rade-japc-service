package cern.rade.services.japc.security;

import cern.rbac.common.RbaToken;
import cern.rbac.common.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Same authorities as rbac-util's {@code RbacAuthenticationService}, which is not available for the jakarta servlet API.
 */
public class LocalRbacTokenSupport implements RbacTokenSupport {

    @Override
    public List<GrantedAuthority> authorities(RbaToken token) {
        Stream<String> location = token.getLocation() == null
                ? Stream.empty()
                : Stream.of(LOCATION_PREFIX + token.getLocation().getName());
        Stream<String> roles = Arrays.stream(token.getUser().getRoles()).map(Role::getName).map(RbacTokenSupport::roleAuthority);
        return Stream.concat(location, roles).<GrantedAuthority>map(SimpleGrantedAuthority::new).toList();
    }
}
