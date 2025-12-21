package online.eracodes.secureenrollmentservice.protobuf;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import online.eracodes.protobuf.enrollment.EnrollmentProto;
import online.eracodes.secureenrollmentservice.crypto.DilithiumSigner;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

@Component
public class SignedResponseFactory {
    public EnrollmentProto.SignedResponse wrap(@Nullable Message body) {
        if (body == null) {
            return null;
        }

        var bodyBytes = body.toByteArray();
        try {
            var signature = DilithiumSigner.sign(bodyBytes);

            return EnrollmentProto.SignedResponse.newBuilder()
                    .setBody(body.toByteString())
                    .setSignature(ByteString.copyFrom(signature))
                    .build();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }
}
