package online.eracodes.secureenrollmentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for Protobuf support in REST API.
 * Configures HTTP message converters and content negotiation for protobuf.
 */
@Configuration
public class ProtobufConfig implements WebMvcConfigurer {

    /**
     * Creates a Protobuf HTTP message converter.
     * This converter handles serialization/deserialization of protobuf messages.
     */
    @Bean
    public ProtobufHttpMessageConverter protobufHttpMessageConverter() {
        return new ProtobufHttpMessageConverter();
    }

    /**
     * Configures content negotiation to prefer protobuf format.
     * Sets application/x-protobuf as the default media type.
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .defaultContentType(MediaType.APPLICATION_OCTET_STREAM)
                .mediaType("protobuf", MediaType.APPLICATION_OCTET_STREAM)
                .mediaType("x-protobuf", new MediaType("application", "x-protobuf"))
                .favorParameter(false)
                .ignoreAcceptHeader(false);
    }
}
