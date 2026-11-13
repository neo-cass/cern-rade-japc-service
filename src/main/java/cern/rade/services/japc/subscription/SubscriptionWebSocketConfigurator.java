package cern.rade.services.japc.subscription;

import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.ratelimit.RateLimiter;
import cern.rade.services.japc.stats.RequestMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import java.time.Clock;

@Configuration
@EnableWebSocket
public class SubscriptionWebSocketConfigurator implements WebSocketConfigurer {
    private final RequestMetrics requestMetrics;
    private final RateLimiter rateLimiter;
    private final Clock clock;

    public SubscriptionWebSocketConfigurator(RequestMetrics requestMetrics, RateLimiter rateLimiter, Clock clock) {
        this.requestMetrics = requestMetrics;
        this.rateLimiter = rateLimiter;
        this.clock = clock;
    }

    /**
     * SubscriptionManager acts similarly to the PerConnectionWebSocketHandler, but it instead requires the handler
     * type to extend SubscriptionWebSocketHandler. It reads the service properties through the ContextProvider.
     */
    @Bean
    @DependsOn("contextProvider")
    public SubscriptionManager subscriptionManager() {
        SubscriptionManager subscriptionManager = new SubscriptionManager(SubscriptionWebSocketHandler.class);
        //We must initialise so that the manager creates its executors for pinging connected clients
        subscriptionManager.init();
        return subscriptionManager;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //We intercept the HTTP handshake, so we can initialise the SubscriptionService
        //and return the client's RBAC ID and type descriptor as required.
        registry.addHandler(subscriptionManager(), ApiPaths.WS + "/**")
                .addInterceptors(new SubscriptionHandshakeInterceptor(requestMetrics, rateLimiter, subscriptionManager(), clock));
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);
        container.setMaxSessionIdleTimeout(ContextProvider.getServiceProperties().getSocketIdleTimeout());
        return container;
    }

}
