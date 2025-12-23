package online.eracodes.secureenrollmentservice.web;

import com.google.protobuf.Message;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import online.eracodes.protobuf.error.ErrorProto;
import online.eracodes.secureenrollmentservice.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@SuppressWarnings("ALL")
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final static Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle enrollment service exceptions and return a signed error response.
     *
     * @param ex      The enrollment service exception.
     * @param request The HTTP servlet request.
     * @return A ResponseEntity containing the signed error response.
     */
    @ExceptionHandler(EnrollmentServiceException.class)
    public ResponseEntity<Message> handleEnrollmentServiceException(
            EnrollmentServiceException ex,
            HttpServletRequest request) {
        var errorId = "SES_IR" + UUID.randomUUID().toString()
                .replaceAll("[^0-9]", "");

        log.warn("[{}] Service Exception: {}", errorId, request.getRequestURI(), ex);

        var response = this
                .buildResponse(errorId, ex.getErrorCode(), ex.getMessage(), null, request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(CryptographicException.class)
    public ResponseEntity<Message> handleCryptographicException(CryptographicException e, HttpServletRequest request) {
        var errorId = "SES_IE" + UUID.randomUUID().toString()
                .replaceAll("[^0-9]", "");

        log.error("[{}] Cryptographic Exception: {}", errorId, request.getRequestURI(), e);

        var response = this
                .buildResponse(errorId, e.getErrorCode(), e.getMessage(),
                        e.getLocalizedMessage(), request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Message> handleDuplicateEmailException(DuplicateEmailException e, HttpServletRequest request) {
        var errorId = "SES_IE" + UUID.randomUUID().toString()
                .replaceAll("[^0-9]", "");

        log.error("[{}] Email Exception: {}", errorId, request.getRequestURI(), e);

        var response = this
                .buildResponse(errorId, e.getErrorCode(), e.getMessage(),
                        e.getLocalizedMessage(), request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<Message> handleInvalidEmailException(InvalidEmailException e, HttpServletRequest request) {
        var errorId = "SES_IE" + UUID.randomUUID().toString()
                .replaceAll("[^0-9]", "");

        log.error("[{}] InvalidEmail Exception: {}", errorId, request.getRequestURI(), e);

        var response = this
                .buildResponse(errorId, e.getErrorCode(), e.getMessage(),
                        e.getLocalizedMessage(), request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RegistrationCodeInvalidException.class)
    public ResponseEntity<Message> handleRegistrationCodeInvalidException(RegistrationCodeInvalidException e, HttpServletRequest request) {
        var errorId = "SES_IE" + UUID.randomUUID().toString()
                .replaceAll("[^0-9]", "");

        log.error("[{}] RegistrationCodeInvalid Exception: {}", errorId, request.getRequestURI(), e);

        var response = this
                .buildResponse(errorId, e.getErrorCode(), e.getMessage(),
                        e.getLocalizedMessage(), request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<Message> handleRegistrationException(RegistrationException e, HttpServletRequest request) {
        var errorId = "SES_IE" + UUID.randomUUID().toString()
                .replaceAll("[^0-9]", "");

        log.error("[{}] Registration Exception: {}", errorId, request.getRequestURI(), e);

        var response = this
                .buildResponse(errorId, e.getErrorCode(), e.getMessage(),
                        e.getLocalizedMessage(), request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Message> handleUserNotFoundException(UserNotFoundException e, HttpServletRequest request) {
        var errorId = "SES_IE" + UUID.randomUUID().toString()
                .replaceAll("[^0-9]", "");

        log.error("[{}] UserNotFound Exception: {}", errorId, request.getRequestURI(), e);

        var response = this
                .buildResponse(errorId, e.getErrorCode(), e.getMessage(),
                        e.getLocalizedMessage(), request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle unexpected exceptions and return a signed error response with a 500 status.
     *
     * @param ex      The unexpected exception.
     * @param request The HTTP servlet request.
     * @return A ResponseEntity containing the signed error response with a 500 status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Message> handleException(Exception ex, HttpServletRequest request) {
        var errorId = "SES_IRKDS" + UUID.randomUUID().toString()
                .replaceAll("[^0-9]", "");

        log.warn("[{}] Unexpected Exception: {}", errorId, request.getRequestURI(), ex);

        var response = this
                .buildResponse(errorId, 500, "Internal Server Error", null, request.getRequestURI());

        return ResponseEntity.internalServerError().body(response);
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
