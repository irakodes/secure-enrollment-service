package online.eracodes.secureenrollmentservice.exceptions;

import lombok.Getter;

@Getter
public class EnrollmentServiceException extends RuntimeException {
    private final int errorCode;

    public EnrollmentServiceException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public EnrollmentServiceException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}
