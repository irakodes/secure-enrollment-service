package online.eracodes.secureenrollmentservice.service;

import lombok.RequiredArgsConstructor;
import online.eracodes.protobuf.enrollment.EnrollmentProto;
import online.eracodes.secureenrollmentservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final static Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Override
    public EnrollmentProto.CreateUserResponse createUser(EnrollmentProto.CreateUserRequest request) {
        return null;
    }

    @Override
    public EnrollmentProto.RegisterUserResponse registerUser(EnrollmentProto.RegisterUserRequest request) {
        return null;
    }

    @Override
    public EnrollmentProto.PublicKeyResponse getPublicKey() {
        return null;
    }
}
