package online.eracodes.secureenrollmentservice.service;

import online.eracodes.protobuf.enrollment.EnrollmentProto;

public interface IAdminService {
    EnrollmentProto.AdminLoginResponse loginAdmin(EnrollmentProto.AdminLoginRequest request);
    EnrollmentProto.AdminLoginResponse loginAdmin();
}
