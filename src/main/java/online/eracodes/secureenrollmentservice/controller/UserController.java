package online.eracodes.secureenrollmentservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.eracodes.protobuf.enrollment.EnrollmentProto;
import online.eracodes.protobuf.user.UserProto;
import online.eracodes.secureenrollmentservice.entity.User;
import online.eracodes.secureenrollmentservice.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static online.eracodes.secureenrollmentservice.util.StringsUtil.isEmailValid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping(
            value = "/get",
            consumes = {MediaType.APPLICATION_OCTET_STREAM_VALUE},
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE}
    )
    public UserProto.User getUser() {
        return UserProto.User.newBuilder()
                .setId(1)
                .setName("John Doe")
                .setEmail("john.doe@example.com")
                .build();
    }

    @PostMapping(
            value = "/",
            consumes = { MediaType.APPLICATION_OCTET_STREAM_VALUE },
            produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE }
    )
    public ResponseEntity<?> createNewUser(@RequestBody EnrollmentProto.CreateUserRequest request) {
        log.debug("Incoming user creation request: {}", request);
        if (!isEmailValid(request.getEmail())) {
            // Introduce a wrapper to fit the application's requirement
            return ResponseEntity.badRequest().build();
        }
        var user = userRepository.save(mapRequestToUser(request));
        var location = URI.create("/api/users/" + user.getId());

        return ResponseEntity.created(location).build();
    }

    private User mapRequestToUser(EnrollmentProto.CreateUserRequest request) {
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();
    }
}
