package cern.rade.services.japc.security.sso;

import cern.rade.services.japc.rbac.gateway.RbacGateway;
import cern.rade.services.japc.rbac.login.RbacLoginService;
import cern.rade.services.japc.security.AdminLoginCustomizer;
import cern.rade.services.japc.security.RbacTokenSupport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * CERN SSO login for the admin panel, enabled by the {@code sso} profile ({@code application-sso.yml}).
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.security.oauth2.client.registration.cern", name = "client-id")
public class SsoConfiguration {

    @Bean
    public RbacOidcUserService rbacOidcUserService(RbacGateway gateway, RbacLoginService loginService,
                                                   RbacTokenSupport tokenSupport) {
        return new RbacOidcUserService(gateway, loginService, tokenSupport);
    }

    @Bean
    public AdminLoginCustomizer ssoAdminLogin(RbacOidcUserService userService, ClientRegistrationRepository registrations) {
        return http -> http
                .oauth2Login(login -> login.userInfoEndpoint(userInfo -> userInfo.oidcUserService(userService)))
                .logout(logout -> logout.logoutSuccessHandler(new OidcClientInitiatedLogoutSuccessHandler(registrations)));
    }
}
