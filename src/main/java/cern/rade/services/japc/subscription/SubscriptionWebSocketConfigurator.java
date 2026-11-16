package cern.rade.services.japc.subscription;

import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.parameter.ParameterProvider;
import cern.rade.services.japc.ratelimit.RateLimiter;
import cern.rade.services.japc.stats.RequestMetrics;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import java.time.Clock;

/**
 * Registers the subscription WebSocket endpoint and its container limits.
 */
@Configuration
@EnableWebSocket
@EnableConfigurationProperties(SubscriptionProperties.class)
public class SubscriptionWebSocketConfigurator implements WebSocketConfigurer {
    private final RequestMetrics requestMetrics;
    private final RateLimiter rateLimiter;
    private final Clock clock;
    private final SubscriptionProperties properties;
    private final ParameterProvider parameters;

    public SubscriptionWebSocketConfigurator(RequestMetrics requestMetrics, RateLimiter rateLimiter, Clock clock,
                                          SubscriptionProperties properties, ParameterProvider parameters) {
        this.requestMetrics = requestMetrics;
        this.rateLimiter = rateLimiter;
        this.clock = clock;
        this.properties = properties;
        this.parameters = parameters;
    }

    @Bean
    public SubscriptionManager subscriptionManager() {
        return new SubscriptionManager(properties);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //We intercept the HTTP handshake, so we can initialise the SubscriptionService
        //with the client's RBAC session and return the type descriptor as required.
        registry.addHandler(subscriptionManager(), ApiPaths.WS + "/**")
                .addInterceptors(new SubscriptionHandshakeInterceptor(requestMetrics, rateLimiter, subscriptionManager(), clock, properties, parameters));
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);
        container.setMaxSessionIdleTimeout(properties.socketIdleTimeout().toMillis());
        return container;
    }

}
