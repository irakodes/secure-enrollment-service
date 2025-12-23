package online.eracodes.secureenrollmentservice.exceptions;

import online.eracodes.protobuf.error.ErrorProto;

public class UserNotFoundException extends EnrollmentServiceException {
    public UserNotFoundException(String message) {
        super(ErrorProto.ErrorCode.USER_NOT_FOUND_VALUE, message);
    }
}
