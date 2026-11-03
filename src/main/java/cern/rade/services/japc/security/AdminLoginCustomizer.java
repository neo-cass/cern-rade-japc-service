package cern.rade.services.japc.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Adds a browser login (e.g. CERN SSO) to the admin filter chain. Without one, the admin pages only accept the
 * {@code Rbac} header.
 */
@FunctionalInterface
public interface AdminLoginCustomizer {
    void customize(HttpSecurity http) throws Exception;
}
