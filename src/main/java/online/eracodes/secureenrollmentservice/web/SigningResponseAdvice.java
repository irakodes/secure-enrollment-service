package online.eracodes.secureenrollmentservice.web;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
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
            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        // ResponseBodyAdvice<Message> applies to methods that return Message or ResponseEntity<Message>
        // Spring automatically unwraps ResponseEntity, so we check the actual body type
        
        Class<?> bodyType = returnType.getParameterType();
        
        // Direct Message return type
        if (Message.class.isAssignableFrom(bodyType)) {
            log.debug("ResponseBodyAdvice supports Message return type: {}", bodyType.getSimpleName());
            return true;
        }
        
        // For ResponseEntity<?>, check the nested generic type parameter
        if (org.springframework.http.ResponseEntity.class.isAssignableFrom(bodyType)) {
            // Get the nested parameter type (the generic type of ResponseEntity)
            // Use nested() to get the generic type parameter
            MethodParameter nestedParam = returnType.nested();
            Class<?> nestedType = nestedParam.getParameterType();
            if (Message.class.isAssignableFrom(nestedType)) {
                log.debug("ResponseBodyAdvice supports ResponseEntity<{}> return type", nestedType.getSimpleName());
                return true;
            }
        }
        
        log.debug("ResponseBodyAdvice does NOT support return type: {}", bodyType.getSimpleName());
        return false;
    }

    @Override
    public @Nullable Message beforeBodyWrite(
            @Nullable Message body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {
        
        if (body == null) {
            log.warn("Response body is null, cannot sign");
            return null;
        }
        
        log.info("Signing response of type: {}", body.getClass().getSimpleName());
        try {
            var signed = factory.wrap(body);
            
            if (signed == null) {
                log.error("Factory returned null signed response");
                return body; // Return original if signing fails
            }
            
            log.info("Response signed successfully. Returning SignedResponse");
            response.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return signed;
        } catch (Exception e) {
            log.error("Failed to sign response, returning original body", e);
            // Return original body if signing fails to avoid breaking the response
            return body;
        }
    }

    @Override
    public @Nullable Map<String, Object>
    determineWriteHints(
            @Nullable Message body,
            @NonNull MethodParameter returnType, @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType
    ) {
        return ResponseBodyAdvice.super
                .determineWriteHints(body, returnType, selectedContentType, selectedConverterType);
    }
}
