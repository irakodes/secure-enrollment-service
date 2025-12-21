package online.eracodes.secureenrollmentservice.service;

import online.eracodes.protobuf.enrollment.EnrollmentProto;
import online.eracodes.protobuf.login.LoginProto;

public interface IUserService {
    EnrollmentProto.CreateUserResponse createUser(EnrollmentProto.CreateUserRequest request);
    EnrollmentProto.RegisterUserResponse registerUser(EnrollmentProto.RegisterUserRequest request);
    LoginProto.LoginResponse loginUser(LoginProto.Login request);
}
