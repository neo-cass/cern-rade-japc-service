package cern.rade.services.japc.subscription;

import cern.rade.services.japc.ContextProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSocket
public class SubscriptionWebSocketConfigurator implements WebSocketConfigurer {
    private ConcurrentHashMap <String, SubscriptionManager> subscriptionHandlers = new ConcurrentHashMap<>();
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //SubscriptionManager acts similarly to the PerConnectionWebSocketHandler,
        //but it instead requires the handler type to extend SubscriptionWebSocketHandler
        SubscriptionManager subscriptionHandler = new SubscriptionManager(SubscriptionWebSocketHandler.class);
        subscriptionHandlers.put("subscription", subscriptionHandler);
        //We must initialise so that the manager creates its executors for pinging connected clients
        subscriptionHandler.init();
        //We intercept the HTTP handshake, so we can initialise the SubscriptionService
        //and return the client's RBAC ID and type descriptor as required.
        registry.addHandler(subscriptionHandler, "/subscription/**").addInterceptors(new SubscriptionHandshakeInterceptor());
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
