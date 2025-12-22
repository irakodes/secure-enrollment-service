package online.eracodes.secureenrollmentservice.exceptions;

import online.eracodes.protobuf.error.ErrorProto;

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

    public int getErrorCode() { return errorCode; }

    public class RegistrationCodeInvalidException extends EnrollmentServiceException {
        public RegistrationCodeInvalidException(String message) {
            super(ErrorProto.ErrorCode.REGISTRATION_CODE_INVALID_VALUE, message);
        }
    }

    public class CryptographicException extends EnrollmentServiceException {
        public CryptographicException(String message, Throwable cause) {
            super(ErrorProto.ErrorCode.CRYPTOGRAPHIC_ERROR_VALUE, message, cause);
        }
    }

    public class DuplicateEmailException extends EnrollmentServiceException {
        public DuplicateEmailException(String email) {
            super(ErrorProto.ErrorCode.DUPLICATE_EMAIL_VALUE,
                    "User already exists with email: " + email);
        }
    }
}
