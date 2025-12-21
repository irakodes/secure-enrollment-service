package online.eracodes.secureenrollmentservice.web;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.google.protobuf.Message;

import lombok.RequiredArgsConstructor;
import online.eracodes.secureenrollmentservice.protobuf.SignedResponseFactory;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class SigningResponseAdvice implements ResponseBodyAdvice<Message> {

    //private final FactoryBean factoryBean;
    private final SignedResponseFactory factory;

    @Override
    public boolean supports(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        var success = Message.class.isAssignableFrom(returnType.getParameterType());
        return success;
    }

    @Override
    public @Nullable Message beforeBodyWrite(
            @Nullable Message body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request, ServerHttpResponse response) {
        log.info("Calling factory method for response wrapping");
        var signed = factory.wrap(body);

        log.info("Response wrapping completed successfully");
        response.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return (Message) signed;
    }

    @Override
    public @Nullable Map<String, Object>
    determineWriteHints(
            @Nullable Message body,
            MethodParameter returnType, MediaType selectedContentType,
                        Class<? extends HttpMessageConverter<?>> selectedConverterType
    ) {
        return ResponseBodyAdvice.super
                .determineWriteHints(body, returnType, selectedContentType, selectedConverterType);
    }
}
