package cern.rade.services.japc.stats;

import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.ratelimit.RateLimitProperties;
import cern.rade.services.japc.ratelimit.RateLimiter;
import cern.rade.services.japc.rbac.RbacSessionRegistry;
import cern.rade.services.japc.subscription.SubscriptionRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.log4j.appender.v2_17.OpenTelemetryAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class StatsConfiguration implements WebMvcConfigurer {
    private final RequestMetrics metrics;
    private final RateLimiter rateLimiter;

    public StatsConfiguration(RequestMetrics metrics, RateLimiter rateLimiter) {
        this.metrics = metrics;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestInterceptor(metrics, rateLimiter)).addPathPatterns(ApiPaths.API + "/**");
    }

    @Bean
    public MeterBinder japcGauges(SubscriptionRegistry subscriptions, RbacSessionRegistry sessions) {
        return registry -> {
            Gauge.builder("japc.subscriptions.active", subscriptions, s -> s.subscriptions().size()).register(registry);
            Gauge.builder("japc.rbac.sessions.cached", sessions, s -> s.list().size()).register(registry);
        };
    }

    /**
     * Connects the OpenTelemetry appender of log4j2-prod.yml to the SDK Spring Boot configures for OTLP export.
     */
    @Bean
    public InitializingBean otlpLogAppender(OpenTelemetry openTelemetry) {
        return () -> OpenTelemetryAppender.install(openTelemetry);
    }
}
