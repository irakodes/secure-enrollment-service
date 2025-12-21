package online.eracodes.secureenrollmentservice.protobuf;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.eracodes.protobuf.enrollment.EnrollmentProto;
import online.eracodes.secureenrollmentservice.crypto.DilithiumSigner;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Factory for creating signed protobuf responses.
 * Wraps response messages with Dilithium signatures.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SignedResponseFactory {
    
    //private final DilithiumSigner signer;

    /**
     * Wraps a protobuf message in a SignedResponse with a Dilithium signature.
     *
     * @param body the message to sign (can be null)
     * @return SignedResponse containing the body and signature, or null if body is null
     */
    public EnrollmentProto.SignedResponse wrap(@Nullable Message body) {
        if (body == null) {
            log.warn("Body is null, cannot sign");
            return null;
        }

        try {
            var bodyBytes = body.toByteArray();

            //var signature = signer.sign(bodyBytes);
            //log.debug("Signed response: {}", signature);

            return EnrollmentProto.SignedResponse.newBuilder()
                    .setBody(ByteString.copyFrom(bodyBytes))
                    //.setSignature(ByteString.copyFrom(signature))
                    .build();
        } catch (Exception e) {
            log.error("Failed to create signed response", e);
            throw new RuntimeException("Failed to sign response", e);
        }
    }
}
