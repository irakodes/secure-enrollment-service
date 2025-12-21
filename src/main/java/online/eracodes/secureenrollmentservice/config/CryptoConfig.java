package online.eracodes.secureenrollmentservice.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import online.eracodes.secureenrollmentservice.crypto.DilithiumKeyService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;

/**
 * Configuration class for cryptographic services.
 * <p>
 * Registers the BouncyCastle provider and initializes Dilithium key services.
 * Ensures proper setup of post-quantum cryptographic capabilities.
 */
@Slf4j
@Configuration
public class CryptoConfig {

    /**
     * Registers the BouncyCastle provider with the Java Security framework.
     * This must be done before any cryptographic operations are performed.
     */
    @PostConstruct
    public void registerBouncyCastleProvider() {

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) != null) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
            log.debug("Removed existing BouncyCastle provider to avoid version conflicts");
        }
        
        // Registration of the BouncyCastle provider ** Take 12 **
        Security.addProvider(new BouncyCastleProvider());
        log.debug("BouncyCastle provider registered successfully");
        log.info("BouncyCastle provider configured for post-quantum cryptography");
    }

    /**
     * Creates and initializes the DilithiumKeyService bean.
     * The service will generate or load keys on first use.
     * 
     * @param keyDirectory Directory where keys will be stored
     * @param securityLevel Security level (2, 3, or 5)
     * @return Configured DilithiumKeyService instance
     * @throws GeneralSecurityException If key service initialization fails
     * @throws IOException If key directory cannot be created
     */
    @Bean
    public DilithiumKeyService dilithiumKeyService(
            @Value("${dilithium.key.directory:./data/keys}") String keyDirectory,
            @Value("${dilithium.security.level:2}") int securityLevel) 
            throws GeneralSecurityException, IOException {
        log.info("Initializing DilithiumKeyService with security level {}...", securityLevel);
        var keyService = new DilithiumKeyService(keyDirectory, securityLevel);
        
        // Ensure key pair exists (will generate if needed)
        try {
            keyService.getKeyPair();
            log.info("Dilithium key pair ready");
        } catch (Exception e) {
            log.error("Failed to initialize Dilithium key pair", e);
            throw e;
        }
        
        // Persist keys to disk
        try {
            keyService.saveKeyPair();
            log.info("Dilithium keys persisted to disk");
        } catch (Exception e) {
            log.warn("Failed to persist keys to disk (keys will be regenerated on restart)", e);
        }
        
        return keyService;
    }
}
