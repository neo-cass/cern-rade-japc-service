package cern.rade.services.japc.subscription;

import cern.rade.services.japc.ContextProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class SubscriptionConfigurator implements WebSocketConfigurer {
    private final long socketIdleTimeout = ContextProvider.getServiceProperties().getSocketIdleTimeout();
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        HttpSessionHandshakeInterceptor HandshakeInterceptor = new HttpSessionHandshakeInterceptor();
        registry.addHandler(new SubscriptionWebsocketHandler(), "/test/subscription").addInterceptors(new HandshakeInterceptor());
    }
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);
        container.setMaxSessionIdleTimeout(socketIdleTimeout);
        return container;
    }

}
