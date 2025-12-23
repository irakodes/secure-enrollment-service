package online.eracodes.secureenrollmentservice.exceptions;

import online.eracodes.protobuf.error.ErrorProto;

public class RegistrationCodeInvalidException extends EnrollmentServiceException {
        public RegistrationCodeInvalidException(String message) {
            super(ErrorProto.ErrorCode.REGISTRATION_CODE_INVALID_VALUE, message);
        }
    }