package online.eracodes.secureenrollmentservice.crypto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.*;

/**
 * Service for creating and verifying Dilithium digital signatures.
 * 
 * Implements signing and verification operations following NIST FIPS 204 standards.
 * Uses BouncyCastle's post-quantum cryptography provider for Dilithium operations.
 * 
 * This implementation ensures:
 * - Proper initialization of signature instances
 * - Secure signing with private keys
 * - Reliable verification with public keys
 * - Exception handling for cryptographic operations
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DilithiumSigner {

    private static final String SIGNATURE_ALGORITHM = "Dilithium";
    private static final String PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;
    
    private final DilithiumKeyService keyService;

    /**
     * Signs a message using the Dilithium private key.
     * 
     * The signing process follows NIST FIPS 204 specifications:
     * 1. Initialize the signature instance with the private key
     * 2. Update the signature with the message bytes
     * 3. Generate the signature
     * 
     * @param message The message bytes to sign
     * @return The digital signature bytes
     * @throws GeneralSecurityException If signing fails (e.g., key not available, algorithm error)
     */
    public byte[] sign(byte[] message) throws GeneralSecurityException {
        if (message == null || message.length == 0) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }

        try {
            // Get the private key
            PrivateKey privateKey = keyService.getPrivateKey();
            
            // Initialize signature instance with Dilithium algorithm
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER_NAME);
            signature.initSign(privateKey, new SecureRandom());
            
            // Update with message bytes
            signature.update(message);
            
            // Generate signature
            byte[] signatureBytes = signature.sign();
            
            log.debug("Message signed successfully. Signature length: {} bytes", signatureBytes.length);
            return signatureBytes;
            
        } catch (NoSuchAlgorithmException e) {
            log.error("Dilithium algorithm not available in provider", e);
            throw new GeneralSecurityException("Dilithium signature algorithm not available", e);
        } catch (NoSuchProviderException e) {
            log.error("BouncyCastle provider not available", e);
            throw new GeneralSecurityException("BouncyCastle provider not available", e);
        } catch (InvalidKeyException e) {
            log.error("Invalid private key for signing", e);
            throw new GeneralSecurityException("Invalid private key", e);
        } catch (SignatureException e) {
            log.error("Error during signature generation", e);
            throw new GeneralSecurityException("Signature generation failed", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verifies a digital signature against a message using the Dilithium public key.
     * 
     * The verification process follows NIST FIPS 204 specifications:
     * 1. Initialize the signature instance with the public key
     * 2. Update the signature with the message bytes
     * 3. Verify the signature
     * 
     * @param message The original message bytes
     * @param signatureBytes The signature bytes to verify
     * @return true if the signature is valid, false otherwise
     * @throws GeneralSecurityException If verification fails due to cryptographic errors
     */
    public boolean verify(byte[] message, byte[] signatureBytes) throws GeneralSecurityException {
        if (message == null || message.length == 0) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        if (signatureBytes == null || signatureBytes.length == 0) {
            throw new IllegalArgumentException("Signature cannot be null or empty");
        }

        try {
            // Get the public key
            PublicKey publicKey = keyService.getPublicKey();
            
            // Initialize signature instance for verification
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER_NAME);
            signature.initVerify(publicKey);
            
            // Update with message bytes
            signature.update(message);
            
            // Verify signature
            boolean isValid = signature.verify(signatureBytes);
            
            log.debug("Signature verification result: {}", isValid);
            return isValid;
            
        } catch (NoSuchAlgorithmException e) {
            log.error("Dilithium algorithm not available in provider", e);
            throw new GeneralSecurityException("Dilithium signature algorithm not available", e);
        } catch (NoSuchProviderException e) {
            log.error("BouncyCastle provider not available", e);
            throw new GeneralSecurityException("BouncyCastle provider not available", e);
        } catch (InvalidKeyException e) {
            log.error("Invalid public key for verification", e);
            throw new GeneralSecurityException("Invalid public key", e);
        } catch (SignatureException e) {
            log.error("Error during signature verification", e);
            throw new GeneralSecurityException("Signature verification failed", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Signs a message using a provided private key (for advanced use cases).
     * 
     * @param message The message bytes to sign
     * @param privateKey The private key to use for signing
     * @return The digital signature bytes
     * @throws GeneralSecurityException If signing fails
     */
    public byte[] sign(byte[] message, PrivateKey privateKey) throws GeneralSecurityException {
        if (message == null || message.length == 0) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        if (privateKey == null) {
            throw new IllegalArgumentException("Private key cannot be null");
        }

        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER_NAME);
            signature.initSign(privateKey, new SecureRandom());
            signature.update(message);
            return signature.sign();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | SignatureException e) {
            log.error("Error signing message with provided key", e);
            throw new GeneralSecurityException("Signature generation failed", e);
        }
    }

    /**
     * Verifies a signature using a provided public key (for advanced use cases).
     * 
     * @param message The original message bytes
     * @param signatureBytes The signature bytes to verify
     * @param publicKey The public key to use for verification
     * @return true if the signature is valid, false otherwise
     * @throws GeneralSecurityException If verification fails
     */
    public boolean verify(byte[] message, byte[] signatureBytes, PublicKey publicKey) throws GeneralSecurityException {
        if (message == null || message.length == 0) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        if (signatureBytes == null || signatureBytes.length == 0) {
            throw new IllegalArgumentException("Signature cannot be null or empty");
        }
        if (publicKey == null) {
            throw new IllegalArgumentException("Public key cannot be null");
        }

        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER_NAME);
            signature.initVerify(publicKey);
            signature.update(message);
            return signature.verify(signatureBytes);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | SignatureException e) {
            log.error("Error verifying signature with provided key", e);
            throw new GeneralSecurityException("Signature verification failed", e);
        }
    }
}
