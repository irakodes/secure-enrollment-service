package online.eracodes.secureenrollmentservice.controller;

import online.eracodes.protobuf.user.UserProto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping(
            consumes = { MediaType.APPLICATION_OCTET_STREAM_VALUE },
            produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE }
    )
    public UserProto.User getUser() {
        return UserProto.User.newBuilder()
                .setId(1)
                .setName("John Doe")
                .setEmail("john.doe@example.com")
                .build();
    }

    @GetMapping("/test")
    public String test() {
        return "Server is running! H2 console should be at /h2-console";
    }
}
