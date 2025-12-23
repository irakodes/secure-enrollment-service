package online.eracodes.secureenrollmentservice.exceptions;

import online.eracodes.protobuf.error.ErrorProto;

public class RegistrationException extends EnrollmentServiceException {
    public static final String USER_ALREADY_REGISTERED = "0x1";
    public static final String REGISTRATION_CODE_EXPIRED = "0x2";
    public static final String REGISTRATION_CODE_INVALID = "0x3";

    /*
     * 1. User already registered
     * 2. Registration code expired
     * 3. Registration code invalid
     * 4. User is blacklisted/blocked
     *
     * */
    public RegistrationException(String xerror) {
        super(mapErrorCode(xerror), mapErrorMessage(xerror));
    }

    public RegistrationException(int errorCode, String message) {
        super(errorCode, message);
    }

    public RegistrationException(int errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    private static int mapErrorCode(String xerror) {
        return switch (xerror) {
            case USER_ALREADY_REGISTERED -> ErrorProto.ErrorCode.USER_NOT_FOUND_VALUE;
            case REGISTRATION_CODE_EXPIRED -> ErrorProto.ErrorCode.REGISTRATION_CODE_EXPIRED_VALUE;
            case REGISTRATION_CODE_INVALID -> ErrorProto.ErrorCode.REGISTRATION_CODE_INVALID_VALUE;
            default -> ErrorProto.ErrorCode.UNKNOWN_ERROR_VALUE;
        };
    }

    private static String mapErrorMessage(String xerror) {
        return switch (xerror) {
            case USER_ALREADY_REGISTERED -> "User already registered";
            case REGISTRATION_CODE_EXPIRED -> "Registration code expired";
            case REGISTRATION_CODE_INVALID -> "Registration code invalid";
            default -> "Registration failed due to an unknown error";
        };
    }
}
