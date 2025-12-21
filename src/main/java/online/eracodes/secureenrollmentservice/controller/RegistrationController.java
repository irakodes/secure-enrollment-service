package online.eracodes.secureenrollmentservice.controller;

import lombok.RequiredArgsConstructor;
import online.eracodes.protobuf.enrollment.EnrollmentProto;
import online.eracodes.secureenrollmentservice.entity.AppUser;
import online.eracodes.secureenrollmentservice.security.RegistrationAuthnProvider;
import online.eracodes.secureenrollmentservice.security.RegistrationAuthnToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/register")
public class RegistrationController {
    private final static Logger LOG = LoggerFactory.getLogger(RegistrationController.class);
    private final RegistrationAuthnProvider registrationAuthnProvider;

    @PostMapping(
            value = "/",
            consumes = { MediaType.APPLICATION_OCTET_STREAM_VALUE },
            produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE }
    )
    public ResponseEntity<?> registerUser(@RequestBody EnrollmentProto.CreateUserRequest request) {
        LOG.debug("Incoming request: {}", request);
        throw new UnsupportedOperationException("Registration endpoint not yet implemented.");
    }

    @PostMapping(
            value = "/complete",
            consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE }
    )
    public ResponseEntity<EnrollmentProto.RegisterUserResponse> completeRegistration(
            @RequestBody EnrollmentProto.RegisterUserRequest request) {
        LOG.debug("Registration request for email: {}", request.getEmail());

        try {
            var authToken = new RegistrationAuthnToken(
                    request.getEmail(),
                    request.getRegistrationCode()
            );

            var authenticatedToken = (RegistrationAuthnToken) registrationAuthnProvider
                    .authenticate(authToken);

            var appUser = (AppUser) authenticatedToken.getPrincipal();

            var response = EnrollmentProto.RegisterUserResponse.newBuilder()
                    .setAuthenticationToken(appUser.getAuthToken())
                    .setUserName(appUser.getEmail())
                    .setUserEmail(appUser.getEmail())
                    .setMessage("Registration completed successfully")
                    .build();

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            LOG.error("Registration failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error("Unexpected error during registration", e);
            throw new RuntimeException("Registration failed", e);
        }
    }
}