package online.eracodes.secureenrollmentservice.exceptions;

import online.eracodes.protobuf.error.ErrorProto;

public class DuplicateEmailException extends EnrollmentServiceException {
        public DuplicateEmailException(String email) {
            super(ErrorProto.ErrorCode.DUPLICATE_EMAIL_VALUE,
                    "User already exists with email: " + email);
        }
    }