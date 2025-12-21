package online.eracodes.secureenrollmentservice.controller;

import com.google.protobuf.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.eracodes.protobuf.enrollment.EnrollmentProto;
import online.eracodes.protobuf.user.UserProto;
import online.eracodes.secureenrollmentservice.service.IUserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final IUserService userService;

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
            consumes = {MediaType.APPLICATION_OCTET_STREAM_VALUE},
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE}
    )
    public ResponseEntity<Message> createNewUser(@RequestBody EnrollmentProto.CreateUserRequest request) {
        var response = userService.createUser(request);

        //TODO: This should be an HTTP 201 Created
        return ResponseEntity.ok(response);
    }
}
