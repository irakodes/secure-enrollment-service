package online.eracodes.secureenrollmentservice.exceptions;

import online.eracodes.protobuf.error.ErrorProto;

public class InvalidEmailException extends EnrollmentServiceException {
        public InvalidEmailException(String message) {
            super(ErrorProto.ErrorCode.INVALID_EMAIL_FORMAT_VALUE, message);
        }
    }