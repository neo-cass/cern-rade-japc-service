package cern.rade.services.japc.security;

import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.rbac.RbacHeaders;
import cern.rade.services.japc.rbac.RbacProperties;
import cern.rade.services.japc.rbac.RbacSessionRegistry;
import cern.rade.services.japc.rbac.gateway.RbacGateway;
import cern.rade.services.japc.rbac.login.RbacLoginService;
import cern.rade.services.japc.security.sso.RbacOidcUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.stream.Stream;

/**
 * Two filter chains: the admin panel and admin API (browser session or {@code Rbac: Cached} header,
 * admin role), and everything else (stateless, {@code Rbac} header, user role for the API and subscriptions, user or
 * admin role for {@code /rbac/session}, the only place where a client logs in).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private static final String[] PUBLIC_PATHS = {
            "/", "/index.html", "/common.css", "/favicon.ico", "/logos/**", "/docs", "/docs/**", "/v3/api-docs/**",
            "/openapi.yaml", "/asyncapi.yaml", "/error", "/readyz", "/actuator/health/**"};
    private static final String[] LOGIN_PATHS = {"/oauth2/**", "/login/**"};
    private static final String LOGIN_REQUIRED = "RBAC login required: log in with PUT " + ApiPaths.RBAC_SESSION
            + " (Rbac: Explicit, Kerberos, Oauth or Token), then send Rbac: Cached <id>";

    private final RbacLoginService loginService;
    private final RbacSessionRegistry registry;
    private final RbacProperties properties;

    public SecurityConfiguration(RbacLoginService loginService, RbacSessionRegistry registry, RbacProperties properties) {
        this.loginService = loginService;
        this.registry = registry;
        this.properties = properties;
    }

    /**
     * The only place to change when rbac-util ships a jakarta {@code RbacAuthenticationService}.
     */
    @Bean
    public RbacTokenSupport rbacTokenSupport() {
        return new LocalRbacTokenSupport();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http, RbacTokenSupport tokenSupport, RbacGateway gateway,
                                                ObjectProvider<ClientRegistrationRepository> ssoRegistrations) throws Exception {
        String role = properties.adminRole();
        http.securityMatcher(ApiPaths.ADMIN, ApiPaths.ADMIN + "/**", "/oauth2/**", "/login/**", "/logout")
                .addFilterAfter(new InactiveSessionFilter(), SecurityContextHolderFilter.class)
                // no login with credentials here: the panel logs in with CERN SSO, API clients use /rbac/session
                .addFilterBefore(rbacHeaderFilter(tokenSupport, request -> false), AnonymousAuthenticationFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(csrf -> csrf.spa()
                        .ignoringRequestMatchers(SecurityConfiguration::carriesRbacCredentials)
                        .ignoringRequestMatchers("/logout"))
                .logout(logout -> logout
                        .addLogoutHandler((request, response, authentication) ->
                                RbacAuthentication.of(authentication).ifPresent(registry::revoke))
                        .logoutSuccessUrl("/"))
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(SecurityErrorWriter.unauthorized(LOGIN_REQUIRED),
                                PathPatternRequestMatcher.withDefaults().matcher(ApiPaths.ADMIN_API + "/**"))
                        .accessDeniedHandler(SecurityErrorWriter.forbidden(roleRequired(role))))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(LOGIN_PATHS).permitAll()
                        .anyRequest().hasAuthority(RbacTokenSupport.roleAuthority(role)));
        ClientRegistrationRepository registrations = ssoRegistrations.getIfAvailable();
        if (registrations != null) {
            addSsoLogin(http, registrations, new RbacOidcUserService(gateway, loginService, tokenSupport));
        }
        return http.build();
    }

    /**
     * CERN SSO login for the admin panel. Only when the {@code sso} profile configures the client
     * ({@code application-sso.yml}): local runs and CI tests have no SSO credentials.
     */
    private static void addSsoLogin(HttpSecurity http, ClientRegistrationRepository registrations,
                                    RbacOidcUserService userService) throws Exception {
        http.oauth2Login(login -> login.userInfoEndpoint(userInfo -> userInfo.oidcUserService(userService)))
                .logout(logout -> logout.logoutSuccessHandler(new OidcClientInitiatedLogoutSuccessHandler(registrations)));
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http, RbacTokenSupport tokenSupport) throws Exception {
        String role = properties.requiredRole();
        RequestMatcher loginRequest = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.PUT, ApiPaths.RBAC_SESSION);
        return http
                .addFilterBefore(rbacHeaderFilter(tokenSupport, loginRequest), AnonymousAuthenticationFilter.class)
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
                        .requestMatchers(ApiPaths.RBAC + "/**").hasAnyAuthority(userOrAdminAuthorities())
                        .anyRequest().denyAll())
                .build();
    }

    /**
     * Distinct, because both roles may be configured to the same one and Spring rejects duplicate authorities.
     */
    private String[] userOrAdminAuthorities() {
        return Stream.of(properties.requiredRole(), properties.adminRole()).distinct()
                .map(RbacTokenSupport::roleAuthority).toArray(String[]::new);
    }

    /**
     * Not a bean: Spring Boot would also register a filter bean for every request, outside the chains.
     */
    private RbacHeaderFilter rbacHeaderFilter(RbacTokenSupport tokenSupport, RequestMatcher loginRequest) {
        return new RbacHeaderFilter(loginService, tokenSupport, registry, loginRequest);
    }

    /**
     * Header credentials cannot be sent cross-site without CORS, so such requests need no CSRF token.
     */
    private static boolean carriesRbacCredentials(HttpServletRequest request) {
        return request.getHeader(RbacHeaders.HEADER) != null;
    }

    private static String roleRequired(String role) {
        return "RBAC role " + role + " required";
    }
}
