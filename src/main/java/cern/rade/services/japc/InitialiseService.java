package cern.rade.services.japc;

import cern.rade.services.japc.parameter.ParameterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point of the JAPC REST and WebSocket service.
 */
@SpringBootApplication
@EnableScheduling
public class InitialiseService {
    private static final Logger logger = LoggerFactory.getLogger(InitialiseService.class);
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception in thread {}.", t, e));
        SpringApplication.run(InitialiseService.class, args);
        logger.info("Service initialised.");
    }

    @Bean
    public ParameterProvider parameterProvider() {
        return ParameterProvider.japc();
    }
}

