package online.eracodes.secureenrollmentservice.controller;

import lombok.RequiredArgsConstructor;
import online.eracodes.protobuf.enrollment.EnrollmentProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/register")
public class RegistrationController {
    private final static Logger LOG = LoggerFactory.getLogger(RegistrationController.class);

    @PostMapping(
            value = "/",
            consumes = { MediaType.APPLICATION_OCTET_STREAM_VALUE },
            produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE }
    )
    public ResponseEntity<?> registerUser(@RequestBody EnrollmentProto.CreateUserRequest request) {
        LOG.debug("Incoming request: {}", request);
        throw new UnsupportedOperationException("Registration endpoint not yet implemented.");
    }
}
