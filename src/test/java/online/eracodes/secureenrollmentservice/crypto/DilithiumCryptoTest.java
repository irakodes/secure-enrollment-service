package online.eracodes.secureenrollmentservice.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

class DilithiumCryptoTest {

    @TempDir
    Path tempDir;

    @BeforeAll
    static void setup() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Test
    void testKeyGenerationAndSigning() throws Exception {
        // 1. Initialize Key Service
        DilithiumKeyService keyService = new DilithiumKeyService(tempDir.toString(), 2);
        
        // 2. Generate Keys
        var keyPair = keyService.generateKeyPair();
        assertNotNull(keyPair);
        assertNotNull(keyPair.getPrivate());
        assertNotNull(keyPair.getPublic());

        // 3. Initialize Signer
        DilithiumSigner signer = new DilithiumSigner(keyService);

        // 4. Sign a message
        String message = "Hello, Post-Quantum World!";
        byte[] messageBytes = message.getBytes();
        byte[] signature = signer.sign(messageBytes);

        assertNotNull(signature);
        assertTrue(signature.length > 0);

        // 5. Verify the signature
        boolean isValid = signer.verify(messageBytes, signature);
        assertTrue(isValid, "Signature should be valid");

        // 6. Verify with tampered message
        byte[] tamperedMessage = "Hello, Post-Quantum World?".getBytes();
        boolean isTamperedValid = signer.verify(tamperedMessage, signature);
        assertFalse(isTamperedValid, "Signature should be invalid for tampered message");
    }
}
