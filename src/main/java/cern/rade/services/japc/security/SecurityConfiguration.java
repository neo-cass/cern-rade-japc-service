package cern.rade.services.japc.security;

import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.rbac.RbacHeaders;
import cern.rade.services.japc.rbac.RbacProperties;
import cern.rade.services.japc.rbac.RbacSessionFactory;
import cern.rade.services.japc.rbac.gateway.RbacGateway;
import cern.rade.services.japc.rbac.login.RbacLoginService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * Two filter chains: the admin panel and admin API (browser session or {@code Rbac} header, admin role), and
 * everything else (stateless, {@code Rbac} header, user role for the API and subscriptions).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private static final String[] PUBLIC_PATHS = {
            "/", "/index.html", "/common.css", "/favicon.ico", "/logos/**", "/docs", "/docs/**", "/v3/api-docs/**",
            "/openapi.yaml", "/asyncapi.yaml", "/error", "/readyz", "/actuator/health/**"};
    private static final String[] LOGIN_PATHS = {"/oauth2/**", "/login/**"};
    private static final String LOGIN_REQUIRED = "RBAC login required: send an Rbac header (Explicit, Kerberos, Location or Cached)";

    private final RbacLoginService loginService;
    private final RbacSessionFactory sessions;
    private final RbacProperties properties;

    public SecurityConfiguration(RbacLoginService loginService, RbacSessionFactory sessions, RbacProperties properties) {
        this.loginService = loginService;
        this.sessions = sessions;
        this.properties = properties;
    }

    /**
     * The only place to change when rbac-util ships a jakarta {@code RbacAuthenticationService}.
     */
    @Bean
    public RbacTokenSupport rbacTokenSupport(RbacGateway gateway) {
        return new LocalRbacTokenSupport(gateway);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http, RbacTokenSupport tokenSupport,
                                                ObjectProvider<AdminLoginCustomizer> adminLogins) throws Exception {
        String role = properties.adminRole();
        http.securityMatcher(ApiPaths.ADMIN, ApiPaths.ADMIN + "/**", "/oauth2/**", "/login/**", "/logout")
                .addFilterBefore(rbacHeaderFilter(tokenSupport), AnonymousAuthenticationFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(csrf -> csrf.spa()
                        .ignoringRequestMatchers(SecurityConfiguration::carriesRbacCredentials)
                        .ignoringRequestMatchers("/logout"))
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(SecurityErrorWriter.unauthorized(LOGIN_REQUIRED),
                                PathPatternRequestMatcher.withDefaults().matcher(ApiPaths.ADMIN_API + "/**"))
                        .accessDeniedHandler(SecurityErrorWriter.forbidden(roleRequired(role))))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(LOGIN_PATHS).permitAll()
                        .anyRequest().hasAuthority(RbacTokenSupport.roleAuthority(role)));
        for (AdminLoginCustomizer adminLogin : adminLogins) {
            adminLogin.customize(http);
        }
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http, RbacTokenSupport tokenSupport) throws Exception {
        String role = properties.requiredRole();
        return http
                .addFilterBefore(rbacHeaderFilter(tokenSupport), AnonymousAuthenticationFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(SecurityErrorWriter.unauthorized(LOGIN_REQUIRED))
                        .accessDeniedHandler(SecurityErrorWriter.forbidden(roleRequired(role))))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(ApiPaths.API + "/**", ApiPaths.WS + "/**").hasAuthority(RbacTokenSupport.roleAuthority(role))
                        .anyRequest().denyAll())
                .build();
    }

    /**
     * Not a bean: Spring Boot would also register a filter bean for every request, outside the chains.
     */
    private RbacHeaderFilter rbacHeaderFilter(RbacTokenSupport tokenSupport) {
        return new RbacHeaderFilter(loginService, tokenSupport, sessions);
    }

    /**
     * Header credentials cannot be sent cross-site without CORS, so such requests need no CSRF token.
     */
    private static boolean carriesRbacCredentials(HttpServletRequest request) {
        return request.getHeader(RbacHeaders.HEADER) != null || request.getHeader(HttpHeaders.AUTHORIZATION) != null;
    }

    private static String roleRequired(String role) {
        return "RBAC role " + role + " required";
    }
}
