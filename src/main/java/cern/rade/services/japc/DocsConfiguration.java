package cern.rade.services.japc;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DocsConfiguration implements WebMvcConfigurer {
    private static final String LOCATION = "classpath:/api-docs/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/openapi.yaml", "/asyncapi.yaml", "/docs/**").addResourceLocations(LOCATION);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/docs").setViewName("forward:/docs/index.html");
        registry.addViewController("/docs/websocket").setViewName("forward:/docs/websocket/index.html");
    }
}
