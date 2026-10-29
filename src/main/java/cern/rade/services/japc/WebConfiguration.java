package cern.rade.services.japc;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    private static final String API_DOCS = "classpath:/api-docs/";
    private static final String WEB = "classpath:/web/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/openapi.yaml", "/asyncapi.yaml", "/docs/**").addResourceLocations(API_DOCS);
        registry.addResourceHandler("/index.html").addResourceLocations(WEB);
        registry.addResourceHandler("/logos/**").addResourceLocations(WEB + "logos/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/docs").setViewName("forward:/docs/index.html");
        registry.addViewController("/docs/websocket").setViewName("forward:/docs/websocket/index.html");
    }
}
