package cern.rade.services.japc.rbac;

import cern.rade.services.japc.rbac.gateway.RbacGateway;
import cern.rade.services.japc.rbac.login.CachedLogin;
import cern.rade.services.japc.rbac.login.ExplicitLogin;
import cern.rade.services.japc.rbac.login.KerberosLogin;
import cern.rade.services.japc.rbac.login.OauthLogin;
import cern.rade.services.japc.rbac.login.RbacLogin;
import cern.rade.services.japc.rbac.login.RbacLoginService;
import cern.rade.services.japc.rbac.login.TokenLogin;
import cern.rbac.client.ClientConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.List;

@Configuration
@EnableConfigurationProperties(RbacProperties.class)
public class RbacConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public RbacGateway rbacGateway() {
        return new RbacGateway(ClientConfiguration.create());
    }

    @Bean
    public RbacSessionFactory rbacSessionFactory(RbacGateway gateway, RbacProperties properties, Clock clock) {
        return new RbacSessionFactory(gateway, properties, clock);
    }

    @Bean
    public RbacSessionRegistry rbacSessionRegistry(RbacProperties properties, Clock clock) {
        return new RbacSessionRegistry(properties, clock);
    }

    @Bean
    public ExplicitLogin explicitLogin(RbacGateway gateway, RbacSessionFactory sessions, RbacProperties properties) {
        return new ExplicitLogin(gateway, sessions, properties);
    }

    @Bean
    public KerberosLogin kerberosLogin(RbacGateway gateway, RbacSessionFactory sessions, RbacProperties properties) {
        return new KerberosLogin(gateway, sessions, properties);
    }

    @Bean
    public OauthLogin oauthLogin(RbacGateway gateway, RbacSessionFactory sessions, RbacProperties properties) {
        return new OauthLogin(gateway, sessions, properties);
    }

    @Bean
    public TokenLogin tokenLogin(RbacGateway gateway, RbacSessionFactory sessions) {
        return new TokenLogin(gateway, sessions);
    }

    @Bean
    public CachedLogin cachedLogin(RbacSessionRegistry registry) {
        return new CachedLogin(registry);
    }

    @Bean
    public RbacLoginService rbacLoginService(List<RbacLogin> logins, RbacSessionRegistry registry) {
        return new RbacLoginService(logins, registry);
    }
}
