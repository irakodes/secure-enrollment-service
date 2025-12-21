package online.eracodes.secureenrollmentservice.util;

import lombok.extern.slf4j.Slf4j;
import online.eracodes.secureenrollmentservice.entity.User;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

import static online.eracodes.secureenrollmentservice.util.EmailValidatorUtil.validateOrThrow;

@Slf4j
public class StringsUtil {

    /**
     * Handles MD5 Hash and returns the last two bytes of the hash
     *
     * @param hash The MD5 Hash Value
     * @return The last two bytes of the MD5 Hash
     */
    private static String md5HashHandler(byte[] hash) {
        // Get the last two bytes of the MD5 Hash value
        var secondToLastByte = hash[hash.length - 2] & 0xFF;
        var lastByte = hash[hash.length - 1] & 0xFF;

        //var checksum = String.format("%02x", secondToLastByte) +
        // String.format("%02x", lastByte);

        //if (checksum.isEmpty()) throw new IllegalArgumentException("Invalid registration code");
        //if (checksum.length() > 4) throw new IllegalArgumentException("Checksum length cannot exceed 4 characters");

        // At this point I am sure the length is valid
        log.debug("Checksum: {}", String.format("%02x%02x", secondToLastByte, lastByte));
        return String.format("%02x%02x", secondToLastByte, lastByte);
    }

    /**
     * Generates a random hexadecimal code of the specified length.
     *
     * @param length The length of the hexadecimal code to generate
     *               <b>Note:</b> The application currently expects 20 characters
     * @return A random hexadecimal code
     */
    private static String generateHexadecimalCode(int length) {
        if (length <= 0) length = 8;
        var secureRandom = new SecureRandom();
        var byteCount = (length + 1) / 2;
        var randomBytes = new byte[byteCount];
        secureRandom.nextBytes(randomBytes);

        var hex = new StringBuilder(byteCount * 2);
        for (var b : randomBytes) {
            hex.append(String.format("%02x", b));
        }

        return hex.substring(0, length);
    }

    /**
     * Generates the first 16 characters of the registration code based off the username
     *
     * @param username The user's unique username
     * @return A 16 Characters long hexadecimal code
     */
    private static String generateRegistrationCode(String username) {
        var usernameInHex = String.format("%08x", username.hashCode());
        var code = generateHexadecimalCode(8);

        return usernameInHex + code;
    }

    /**
     * Generates the 20-character verified code of the 16-character long registration code
     *
     * @param username The user's unique username
     * @return A 20 Characters long hexadecimal code
     */
    public static String getRegistrationCode(String username) {
        var registrationCode = generateRegistrationCode(username);
        var hashValue = EncryptionUtil.getMD5Hash(registrationCode);
        var checksum = md5HashHandler(hashValue);

        return registrationCode + checksum;
    }

    /**
     * Verifies registration code via check summing
     */
    public static boolean isRegistrationCodeVerified(String code) {
        if (code.length() != 20) return false;
        var registrationCode = code.substring(0, 16);
        log.debug("User's registration code: {}", registrationCode);

        var checksum = code.substring(16);
        log.debug("Checksum to validate: {}", checksum);

        var hashValue = EncryptionUtil.getMD5Hash(registrationCode);

        return checksum.equalsIgnoreCase(md5HashHandler(hashValue));
    }

    public static String tokenGenerator(User user) {
        var time = System.currentTimeMillis();
        var regCode = generateRegistrationCode(user.getEmail());
        var uniqueId = UUID.randomUUID().toString().replaceAll("[^a-zA-Z]", "");

        var token = time + regCode + uniqueId;
        return Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isEmailValid(String email) {
        try { validateOrThrow(email); }
        catch (IllegalArgumentException e) { return false; }
        return true;
    }
}
