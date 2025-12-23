package online.eracodes.secureenrollmentservice.exceptions;

import online.eracodes.protobuf.error.ErrorProto;

public class CryptographicException extends EnrollmentServiceException {
        public CryptographicException(String message, Throwable cause) {
            super(ErrorProto.ErrorCode.CRYPTOGRAPHIC_ERROR_VALUE, message, cause);
        }
    }