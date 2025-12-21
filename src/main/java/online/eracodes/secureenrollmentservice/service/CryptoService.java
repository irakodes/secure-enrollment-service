package online.eracodes.secureenrollmentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.eracodes.protobuf.enrollment.EnrollmentProto;
import online.eracodes.secureenrollmentservice.crypto.DilithiumKeyService;
//import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
//import java.security.PublicKey;

@Slf4j
@Component
@RequiredArgsConstructor
public class CryptoService implements ICryptoService {

    private final DilithiumKeyService dilithiumKeyService;

    @Override
    public EnrollmentProto.PublicKeyResponse getPublicKey() {
        try {
            var publicKey = dilithiumKeyService.getPublicKey();
            var paramSpec = dilithiumKeyService.getParameterSpec();

            var algorithmName = paramSpec.getName().toUpperCase();

            return EnrollmentProto.PublicKeyResponse.newBuilder()
                    .setDilithiumPublicKey(com.google.protobuf.ByteString.copyFrom(publicKey.getEncoded()))
                    .setKeyAlgorithm(algorithmName)
                    .setKeyGeneratedAt(System.currentTimeMillis())
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            log.error("Failed to retrieve Dilithium public key", e);
            throw new RuntimeException("Failed to retrieve public key", e);
        }
    }

}
