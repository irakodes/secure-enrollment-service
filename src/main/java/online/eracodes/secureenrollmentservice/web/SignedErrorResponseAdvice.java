package online.eracodes.secureenrollmentservice.web;

import com.google.protobuf.Message;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import online.eracodes.protobuf.error.ErrorProto;
import online.eracodes.secureenrollmentservice.exceptions.EnrollmentServiceException;
import online.eracodes.secureenrollmentservice.protobuf.SignedResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
@RequiredArgsConstructor
public class SignedErrorResponseAdvice {

    private final static Logger log = LoggerFactory.getLogger(SignedErrorResponseAdvice.class);
    private final SignedResponseFactory responseFactory;

    @ExceptionHandler(EnrollmentServiceException.class)
    public ResponseEntity<Message> handleEnrollmentServiceException(
            EnrollmentServiceException ex,
            HttpServletRequest request) {
        var errorId = "SES_IRKDS" + UUID.randomUUID().toString()
                .replaceAll("[^a-zA-Z]", "");

        log.warn("[{}] Service Exception: {}", errorId, request.getRequestURI(), ex);

        var response = this
                .buildResponse(errorId, ex.getErrorCode(), ex.getMessage(), null, request.getRequestURI());

        return ResponseEntity.badRequest()
                .body(responseFactory.wrap(response));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Message> handleException(Exception ex, HttpServletRequest request) {
        var errorId = "SES_IRKDS" + UUID.randomUUID().toString()
                .replaceAll("[^a-zA-Z]", "");

        log.warn("[{}] Unexpected Exception: {}", errorId, request.getRequestURI(), ex);

        var response = this
                .buildResponse(errorId, 500, "Internal Server Error", null, request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(responseFactory.wrap(response));
    }

    private ErrorProto.ErrorResponse buildResponse(
            String errorId,
            int errorCode,
            String message,
            String details,
            String path) {

        var builder = ErrorProto.ErrorResponse.newBuilder()
                .setErrorId(errorId)
                .setErrorCode(errorCode)
                .setMessage(message)
                .setTimestamp(System.currentTimeMillis())
                .setPath(path);

        if (details != null) {
            builder.setDetails(details);
        }

        return builder.build();
    }
}
