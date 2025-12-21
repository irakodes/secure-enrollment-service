package online.eracodes.secureenrollmentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.eracodes.protobuf.enrollment.EnrollmentProto;
import online.eracodes.protobuf.login.LoginProto;
import online.eracodes.secureenrollmentservice.crypto.DilithiumKeyService;
import online.eracodes.secureenrollmentservice.entity.AppUser;
import online.eracodes.secureenrollmentservice.entity.User;
import online.eracodes.secureenrollmentservice.repository.UserRepository;
import online.eracodes.secureenrollmentservice.security.RegistrationAuthnProvider;
import online.eracodes.secureenrollmentservice.security.RegistrationAuthnToken;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;


import static online.eracodes.secureenrollmentservice.util.StringsUtil.getRegistrationCode;
import static online.eracodes.secureenrollmentservice.util.StringsUtil.isEmailValid;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final PasswordEncoder pwdEncoder;
    private final UserRepository userRepository;
    private final RegistrationAuthnProvider regAuthnProvider;
    private final DilithiumKeyService dilithiumKeyService;

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

        log.info("User {} registration code generated", user.getEmail());
        log.info("""
                {}+-----------------------------------------+
                | CODE: {}              |
                +-----------------------------------------+
                """, System.lineSeparator(), userCode);
        return response;
    }

    @Override
    public EnrollmentProto.RegisterUserResponse registerUser(EnrollmentProto.RegisterUserRequest request) {
        log.debug("Registration request for email: {}", request.getEmail());

        try {
            var authn = new RegistrationAuthnToken(
                    request.getEmail(),
                    request.getRegistrationCode()
            );

            var authenticatedToken = (RegistrationAuthnToken) regAuthnProvider
                    .authenticate(authn);
            log.debug("Authenticated token: {}", authenticatedToken);

            if (authenticatedToken == null) throw new BadCredentialsException("Invalid registration code");

            var appUser = (AppUser) authenticatedToken.getPrincipal();

            var response = EnrollmentProto.RegisterUserResponse.newBuilder()
                    .setAuthToken(appUser.getAuthToken())
                    .setUsername(appUser.getEmail())
                    .setEmail(appUser.getEmail())
                    .setMessage("Registration completed successfully")
                    .build();

            return response;
        } catch (BadCredentialsException e) {
            log.error("Registration failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during registration", e);
            throw new RuntimeException("Registration failed", e);
        }
    }

    /**
     * Authenticates and logs in a user based on the provided login request data.
     *
     * @param request the login request containing the email and registration code
     * @return a LoginResponse indicating the success or failure of the login operation
     * @throws BadCredentialsException if the provided credentials are invalid or the user is not found
     */
    @Override
    public LoginProto.LoginResponse loginUser(LoginProto.Login request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Wrong credentials!"));

        // Token, stored as plain-text is the basis for subsequent authentication
        /*var rat = new RegistrationAuthnToken(
                request.getEmail(),
                user.getRegistrationCode()
        );
        var authn = regAuthnProvider.authenticate(rat);
        log.debug("Authenticated Principal: {}", authn);

        if (authn == null) throw new BadCredentialsException("User was not found!");

        var appUser = (AppUser) authn.getPrincipal();
        log.debug("User Principal Loaded: {}", appUser);*/

        if (user.getAuthToken().equals(request.getToken()))
            return LoginProto.LoginResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Login successful")
                    .build();

        return LoginProto.LoginResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Login failed")
                .build();
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
