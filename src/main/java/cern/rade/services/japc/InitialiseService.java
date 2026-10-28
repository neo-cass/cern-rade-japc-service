package cern.rade.services.japc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
public class InitialiseService {
    private static final Logger logger = LoggerFactory.getLogger(InitialiseService.class);
    public static void main(String[] args) {
        SpringApplication.run(InitialiseService.class, args);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception in thread {}.", t, e));
        logger.info("Service initialised.");
    }
}

