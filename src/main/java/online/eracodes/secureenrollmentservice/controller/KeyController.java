package online.eracodes.secureenrollmentservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.eracodes.protobuf.enrollment.EnrollmentProto;
import online.eracodes.secureenrollmentservice.service.ICryptoService;
import online.eracodes.secureenrollmentservice.web.ExcludeFromSigning;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/keys")
public class KeyController {

    private final ICryptoService cryptoService;

    @ExcludeFromSigning
    @GetMapping("/public")
    public ResponseEntity<EnrollmentProto.PublicKeyResponse> getPublicKey() {
        log.info("Received request to get public key");
        return ResponseEntity.ok(cryptoService.getPublicKey());
    }
}
