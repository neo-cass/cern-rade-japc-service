package cern.rade.services.japc.format;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FormatConfiguration implements WebMvcConfigurer {
    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        Formats.ALL.forEach(format -> builder.addCustomConverter(format.converter()));
    }
}
