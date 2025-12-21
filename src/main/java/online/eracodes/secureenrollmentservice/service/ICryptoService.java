package online.eracodes.secureenrollmentservice.service;

import online.eracodes.protobuf.enrollment.EnrollmentProto;

public interface ICryptoService {
    EnrollmentProto.PublicKeyResponse getPublicKey();
}
