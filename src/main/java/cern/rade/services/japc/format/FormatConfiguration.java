package cern.rade.services.japc.format;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers the converters of all formats with Spring MVC.
 */
@Configuration
public class FormatConfiguration implements WebMvcConfigurer {
    private static final String PROPERTY = "service-properties.max-request-body";

    private final int maxBodyBytes;

    public FormatConfiguration(@Value("${service-properties.max-request-body:16MB}") DataSize maxRequestBody) {
        // the body is read into a single array
        if (maxRequestBody.toBytes() <= 0 || maxRequestBody.toBytes() >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException(PROPERTY + " must be positive and under 2GB, but is " + maxRequestBody);
        }
        this.maxBodyBytes = (int) maxRequestBody.toBytes();
    }

    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        Formats.ALL.forEach(format -> builder.addCustomConverter(format.converter(maxBodyBytes)));
    }
}
