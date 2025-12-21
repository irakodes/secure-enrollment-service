package online.eracodes.secureenrollmentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.eracodes.protobuf.enrollment.EnrollmentProto;
import online.eracodes.secureenrollmentservice.entity.User;
import online.eracodes.secureenrollmentservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


import static online.eracodes.secureenrollmentservice.util.StringsUtil.getRegistrationCode;
import static online.eracodes.secureenrollmentservice.util.StringsUtil.isEmailValid;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder pwdEncoder;

    @Override
    public EnrollmentProto.CreateUserResponse createUser(EnrollmentProto.CreateUserRequest request) {
        log.debug("Incoming user creation request: {}", request);
        if (!isEmailValid(request.getEmail())) {
            // Introduce a wrapper to fit the application's requirement
            throw new IllegalArgumentException("Invalid email address");
        }
        var userCode = getRegistrationCode(request.getEmail());
        var user = mapRequestToUser(request, userCode);

        user = userRepository.save(user);

        var response = EnrollmentProto.CreateUserResponse.newBuilder()
                .setRegistrationCode(userCode)
                .setUserId(String.valueOf(user.getId()))
                .setMessage("User created successfully")
                .build();

        log.info("User {} registration code generated is: {}", user.getEmail(), userCode);

        return response;
    }

    @Override
    public EnrollmentProto.RegisterUserResponse registerUser(EnrollmentProto.RegisterUserRequest request) {
        return null;
    }

    @Override
    public EnrollmentProto.PublicKeyResponse getPublicKey() {
        return null;
    }

    private User mapRequestToUser(EnrollmentProto.CreateUserRequest request, String registrationCode) {
        var codePart = registrationCode.substring(0, 16);
        var hashedPassword = pwdEncoder.encode(codePart);
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .registrationCode(hashedPassword)
                .build();
    }
}
