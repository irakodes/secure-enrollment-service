package online.eracodes.secureenrollmentservice.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;

public class DilithiumSigner {

    public static final byte[] sign(byte[] message) throws NoSuchAlgorithmException, NoSuchProviderException {
        var sig = Signature.getInstance("Dilithium", "BCPQC");
        // sig.initSign(privateKey);
        try {
            sig.update(message);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
        try {
            return sig.sign();
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
    }
}
