package online.eracodes.secureenrollmentservice.config;

import com.google.protobuf.Message;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Configuration for Protobuf support in REST API.
 * Configures HTTP message converters and content negotiation for protobuf.
 * All responses use protobuf format with application/octet-stream content type.
 */
@Configuration
public class ProtobufConfig implements WebMvcConfigurer {
    private final static Logger LOGGER = LoggerFactory.getLogger(ProtobufConfig.class);

    /**
     * Creates a custom Protobuf HTTP message converter that uses application/octet-stream.
     * This converter is to handle serialization/deserialization of protobuf messages.
     */
    @Bean
    public HttpMessageConverter<Message> protobufHttpMessageConverter() {
        return new HttpMessageConverter<Message>() {
            @Override
            public boolean canRead(Class<?> clazz, MediaType mediaType) {
                return Message.class.isAssignableFrom(clazz) &&
                        (mediaType == null || mediaType.isCompatibleWith(MediaType.APPLICATION_OCTET_STREAM));
            }

            @Override
            public boolean canWrite(Class<?> clazz, MediaType mediaType) {
                return Message.class.isAssignableFrom(clazz) &&
                        (mediaType == null || mediaType.isCompatibleWith(MediaType.APPLICATION_OCTET_STREAM));
            }

            @Override
            public List<MediaType> getSupportedMediaTypes() {
                return Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM);
            }

            @Override
            public Message read(@NonNull Class<? extends Message> clazz, @NonNull HttpInputMessage inputMessage)
                    throws IOException, HttpMessageNotReadableException {
                try {
                    var newBuilderMethod = clazz.getMethod("newBuilder");
                    var builder = (Message.Builder) newBuilderMethod.invoke(null);

                    return builder.mergeFrom(inputMessage.getBody()).build();
                } catch (Exception e) {
                    LOGGER.error("An exception occurred in the protobuf message read operation: {}",
                            e.getMessage(), e);

                    throw new HttpMessageNotReadableException("Failed to parse protobuf message", e, inputMessage);
                }
            }

            @Override
            public void write(Message message, MediaType contentType, @NonNull HttpOutputMessage outputMessage)
                    throws IOException, HttpMessageNotWritableException {
                outputMessage.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);
                message.writeTo(outputMessage.getBody());
            }
        };
    }

    /**
     * Configures content negotiation to use application/octet-stream for protobuf.
     * Sets application/octet-stream as the default media type.
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .defaultContentType(MediaType.APPLICATION_OCTET_STREAM)
                .mediaType("protobuf", MediaType.APPLICATION_OCTET_STREAM)
                .favorParameter(false)
                .ignoreAcceptHeader(false);
    }
}
