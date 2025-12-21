package online.eracodes.secureenrollmentservice.crypto;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.springframework.beans.factory.annotation.Value;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Service for managing Dilithium key pairs.
 * <p>
 * Implements key generation, storage, and retrieval following NIST FIPS 204 standards.
 * Supports Dilithium2 (128-bit security), Dilithium3 (192-bit security), and Dilithium5 (256-bit security).
 * <p>
 * Key storage uses PKCS#8 format for private keys and X.509 format for public keys,
 * following industry best practices for key serialization.
 */
@Slf4j
public class DilithiumKeyService {

    private static final String KEY_ALGORITHM = "Dilithium";
    private static final String PRIVATE_KEY_FILE = "dilithium_private.key";
    private static final String PUBLIC_KEY_FILE = "dilithium_public.key";
    
    private final Path keyDirectory;
    private final DilithiumParameterSpec parameterSpec;
    private final KeyFactory keyFactory;
    private final KeyPairGenerator keyPairGenerator;
    
    private KeyPair cachedKeyPair;

    /**
     * Constructs a DilithiumKeyService with the specified configuration.
     * 
     * @param keyDirectoryPath Directory where keys will be stored
     * @param securityLevel Security level: 2 (Dilithium2), 3 (Dilithium3), or 5 (Dilithium5)
     * @throws GeneralSecurityException If key generation or factory initialization fails
     * @throws IOException If key directory cannot be created
     */
    public DilithiumKeyService(
            @Value("${dilithium.key.directory:./data/keys}") String keyDirectoryPath,
            @Value("${dilithium.security.level:2}") int securityLevel) 
            throws GeneralSecurityException, IOException {
        
        // Ensure BouncyCastle provider is registered
        Security.addProvider(new BouncyCastleProvider());
        
        // Determine parameter spec based on security level
        this.parameterSpec = switch (securityLevel) {
            case 2 -> DilithiumParameterSpec.dilithium2;
            case 3 -> DilithiumParameterSpec.dilithium3;
            case 5 -> DilithiumParameterSpec.dilithium5;
            default -> {
                log.warn("Invalid security level {}, defaulting to Dilithium2", securityLevel);
                yield DilithiumParameterSpec.dilithium2;
            }
        };
        
        log.info("Initializing DilithiumKeyService with security level {} ({})", 
                securityLevel, parameterSpec.getName());
        
        // Initialize key factory and generator
        this.keyFactory = KeyFactory.getInstance(KEY_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
        this.keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
        this.keyPairGenerator.initialize(parameterSpec, new SecureRandom());
        
        // Set up key directory
        this.keyDirectory = Paths.get(keyDirectoryPath).toAbsolutePath();
        Files.createDirectories(this.keyDirectory);
        
        log.info("Key directory initialized at: {}", this.keyDirectory);
    }

    /**
     * Generates a new Dilithium key pair using a secure random number generator.
     * The generated keys are cached and can be persisted to disk.
     * 
     * @return A new KeyPair containing Dilithium public and private keys
     * @throws GeneralSecurityException If key generation fails
     */
    public KeyPair generateKeyPair() throws GeneralSecurityException {
        log.info("Generating new Dilithium key pair with parameters: {}", parameterSpec.getName());
        
        var secureRandom = new SecureRandom();
        keyPairGenerator.initialize(parameterSpec, secureRandom);
        
        var keyPair = keyPairGenerator.generateKeyPair();
        this.cachedKeyPair = keyPair;
        
        log.info("Key pair generated successfully. Public key size: {} bytes, Private key size: {} bytes",
                keyPair.getPublic().getEncoded().length,
                keyPair.getPrivate().getEncoded().length);
        
        return keyPair;
    }

    /**
     * Retrieves the public key, loading from cache, disk, or generating a new one if needed.
     * 
     * @return The Dilithium public key
     * @throws GeneralSecurityException If key retrieval or generation fails
     * @throws IOException If key file operations fail
     */
    public PublicKey getPublicKey() throws GeneralSecurityException, IOException {
        if (cachedKeyPair != null) {
            return cachedKeyPair.getPublic();
        }
        
        var publicKeyPath = keyDirectory.resolve(PUBLIC_KEY_FILE);
        if (Files.exists(publicKeyPath)) {
            log.debug("Loading public key from disk: {}", publicKeyPath);
            return loadPublicKey(publicKeyPath);
        }
        
        log.info("No public key found, generating new key pair");
        var keyPair = generateKeyPair();
        return keyPair.getPublic();
    }

    /**
     * Retrieves the private key, loading from cache, disk, or generating a new one if needed.
     * 
     * @return The Dilithium private key
     * @throws GeneralSecurityException If key retrieval or generation fails
     * @throws IOException If key file operations fail
     */
    public PrivateKey getPrivateKey() throws GeneralSecurityException, IOException {
        if (cachedKeyPair != null) {
            return cachedKeyPair.getPrivate();
        }
        
        Path privateKeyPath = keyDirectory.resolve(PRIVATE_KEY_FILE);
        if (Files.exists(privateKeyPath)) {
            log.debug("Loading private key from disk: {}", privateKeyPath);
            return loadPrivateKey(privateKeyPath);
        }
        
        log.info("No private key found, generating new key pair");
        KeyPair keyPair = generateKeyPair();
        return keyPair.getPrivate();
    }

    /**
     * Gets the current key pair, loading from disk or generating if needed.
     * 
     * @return The current KeyPair
     * @throws GeneralSecurityException If key retrieval or generation fails
     * @throws IOException If key file operations fail
     */
    public KeyPair getKeyPair() throws GeneralSecurityException, IOException {
        if (cachedKeyPair != null) {
            return cachedKeyPair;
        }
        
        var privateKeyPath = keyDirectory.resolve(PRIVATE_KEY_FILE);
        var publicKeyPath = keyDirectory.resolve(PUBLIC_KEY_FILE);
        
        if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
            log.debug("Loading key pair from disk");
            PrivateKey privateKey = loadPrivateKey(privateKeyPath);
            PublicKey publicKey = loadPublicKey(publicKeyPath);
            cachedKeyPair = new KeyPair(publicKey, privateKey);
            return cachedKeyPair;
        }
        
        log.info("No key pair found, generating new one");
        return generateKeyPair();
    }

    /**
     * Persists the current key pair to disk in standard formats.
     * Private keys are stored in PKCS#8 format, public keys in X.509 format.
     * 
     * @throws IOException If file operations fail
     * @throws GeneralSecurityException If key encoding fails
     */
    public void saveKeyPair() throws IOException, GeneralSecurityException {
        var keyPair = getKeyPair();
        
        var privateKeyPath = keyDirectory.resolve(PRIVATE_KEY_FILE);
        var publicKeyPath = keyDirectory.resolve(PUBLIC_KEY_FILE);
        
        // Save private key in PKCS#8 format
        try (FileOutputStream fos = new FileOutputStream(privateKeyPath.toFile())) {
            fos.write(keyPair.getPrivate().getEncoded());
            log.info("Private key saved to: {}", privateKeyPath);
        }
        
        // Save public key in X.509 format
        try (FileOutputStream fos = new FileOutputStream(publicKeyPath.toFile())) {
            fos.write(keyPair.getPublic().getEncoded());
            log.info("Public key saved to: {}", publicKeyPath);
        }
        
        // Set restrictive file permissions (owner read/write only)
        try {
            Files.setPosixFilePermissions(privateKeyPath, 
                    java.util.Set.of(
                            java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                            java.nio.file.attribute.PosixFilePermission.OWNER_WRITE));
            Files.setPosixFilePermissions(publicKeyPath,
                    java.util.Set.of(
                            java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                            java.nio.file.attribute.PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException e) {
            // POSIX permissions not supported on this filesystem (e.g., Windows)
            log.debug("POSIX file permissions not supported on this filesystem");
        }
    }

    /**
     * Loads a public key from disk.
     * 
     * @param keyPath Path to the public key file
     * @return The loaded PublicKey
     * @throws IOException If file operations fail
     * @throws GeneralSecurityException If key parsing fails
     */
    private PublicKey loadPublicKey(Path keyPath) throws IOException, GeneralSecurityException {
        try (FileInputStream fis = new FileInputStream(keyPath.toFile())) {
            byte[] keyBytes = fis.readAllBytes();
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            return keyFactory.generatePublic(keySpec);
        }
    }

    /**
     * Loads a private key from disk.
     * 
     * @param keyPath Path to the private key file
     * @return The loaded PrivateKey
     * @throws IOException If file operations fail
     * @throws GeneralSecurityException If key parsing fails
     */
    private PrivateKey loadPrivateKey(Path keyPath) throws IOException, GeneralSecurityException {
        try (FileInputStream fis = new FileInputStream(keyPath.toFile())) {
            byte[] keyBytes = fis.readAllBytes();
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            return keyFactory.generatePrivate(keySpec);
        }
    }

    /**
     * Gets the parameter specification being used.
     *
     * @return The DilithiumParameterSpec
     */
    public DilithiumParameterSpec getParameterSpec() {
        return parameterSpec;
    }
}
