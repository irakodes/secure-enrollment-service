package online.eracodes.secureenrollmentservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.eracodes.protobuf.enrollment.EnrollmentProto;
import online.eracodes.secureenrollmentservice.entity.AppUser;
import online.eracodes.secureenrollmentservice.security.RegistrationAuthnProvider;
import online.eracodes.secureenrollmentservice.security.RegistrationAuthnToken;
import online.eracodes.secureenrollmentservice.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/register")
public class RegistrationController {
    private final IUserService userService;

    /*@PostMapping(
            value = "/",
            consumes = { MediaType.APPLICATION_OCTET_STREAM_VALUE },
            produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE }
    )
    public ResponseEntity<?> registerUser(@RequestBody EnrollmentProto.CreateUserRequest request) {
        LOG.debug("Incoming request: {}", request);
        throw new UnsupportedOperationException("Registration endpoint not yet implemented.");
    }*/

    @PostMapping(
            value = "/complete",
            consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE}
    )
    public ResponseEntity<EnrollmentProto.RegisterUserResponse> completeRegistration(
            @RequestBody EnrollmentProto.RegisterUserRequest request) {
        log.debug("Registration request for email: {}", request.getEmail());
        var response = userService.registerUser(request);
        return ResponseEntity.ok(response);
    }
}