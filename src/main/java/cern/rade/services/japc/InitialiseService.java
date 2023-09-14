package cern.rade.services.japc;

import cern.rade.services.japc.subscription.SubscriptionWebsocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.web.servlet.ServletComponentScan;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
@SpringBootApplication
@ServletComponentScan
public class InitialiseService {
    private static final Logger logger = LoggerFactory.getLogger(InitialiseService.class);
    public static void main(String[] args) {
        SpringApplication.run(InitialiseService.class, args);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception in thread {}.", t, e));
        logger.info("Initialisation complete.");
    }
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
    @Bean
    public SubscriptionWebsocket subscriptionWebsocket(){
        return new SubscriptionWebsocket();
    }
}

