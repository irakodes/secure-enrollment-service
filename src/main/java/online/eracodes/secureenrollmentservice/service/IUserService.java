package online.eracodes.secureenrollmentservice.service;

import online.eracodes.protobuf.enrollment.EnrollmentProto;

public interface IUserService {
    EnrollmentProto.CreateUserResponse createUser(EnrollmentProto.CreateUserRequest request);
    EnrollmentProto.RegisterUserResponse registerUser(EnrollmentProto.RegisterUserRequest request);
}
