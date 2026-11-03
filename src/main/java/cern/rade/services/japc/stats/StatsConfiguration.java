package cern.rade.services.japc.stats;

import cern.rade.services.japc.ApiPaths;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;

@Configuration
public class StatsConfiguration implements WebMvcConfigurer {
    private final ClientStats clientStats;

    public StatsConfiguration(Clock clock) {
        this.clientStats = new ClientStats(clock);
    }

    @Bean
    public ClientStats clientStats() {
        return clientStats;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ClientStatsInterceptor(clientStats)).addPathPatterns(ApiPaths.API + "/**");
    }
}
