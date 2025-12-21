package online.eracodes.secureenrollmentservice.web;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    private final SignedResponseFactory factory;

    /**
     * Handles support validation for response return types in `ResponseEntity<Message>` and `Message`
     * @param returnType The method's return type
     * @param converterType The converter type
     * @return True if the advice should run, false otherwise
     */
    @Override
    public boolean supports(
            MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {

        var bodyType = returnType.getParameterType();
        log.debug("Response Body Type: {}", bodyType.getSimpleName());

        // Message return
        if (Message.class.isAssignableFrom(bodyType)) {
            log.debug("ResponseBodyAdvice supports Message return type: {}", bodyType.getSimpleName());
            return true;
        }

        // ResponseEntity<Message> return
        if (ResponseEntity.class.isAssignableFrom(bodyType)) {
            var genericType = extractGenericTypeFromResponseEntity(returnType);

            if (genericType != null && Message.class.isAssignableFrom(genericType)) {
                log.debug("ResponseBodyAdvice supports ResponseEntity<{}> return type", genericType.getSimpleName());
                return true;
            }
        }

        // TODO: Handle error case scenarios
        log.debug("ResponseBodyAdvice does NOT support return type: {}", bodyType.getSimpleName());
        return false;
    }

    /**
     * Extract the generic type parameter from ResponseEntity<T>
     * For example, extracts Message from ResponseEntity<Message>
     */
    private Class<?> extractGenericTypeFromResponseEntity(MethodParameter returnType) {
        try {
            var genericType = returnType.getGenericParameterType();

            if (genericType instanceof ParameterizedType paramType) {
                var typeArgs = paramType.getActualTypeArguments();

                if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                    return (Class<?>) typeArgs[0];
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract generic type from ResponseEntity", e);
        }

        return null;
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
                return body;
            }

            log.info("Response signed successfully. Returning SignedResponse");
            response.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return signed;
        } catch (Exception e) {
            log.error("Failed to sign response, returning original body", e);
            return body;
        }
    }

    @Override
    public @Nullable Map<String, Object> determineWriteHints(
            @Nullable Message body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType) {
        return ResponseBodyAdvice.super
                .determineWriteHints(body, returnType, selectedContentType, selectedConverterType);
    }
}