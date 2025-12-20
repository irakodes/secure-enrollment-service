package online.eracodes.secureenrollmentservice.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class StringsUtil {

    /**
     * Hashes the registration code generated with MD5 Hash
     *
     * @param registrationCode The generated code
     * @return An MD5 hash of the registration code
     */
    private static byte[] hashRegistrationCode(String registrationCode) {
        return DigestUtils.md5Digest(registrationCode.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Handles MD5 Hash and returns the last two bytes of the hash
     *
     * @param hashValue The MD5 Hash Value
     * @return The last two bytes of the MD5 Hash
     */
    private static String md5HashHandler(byte[] hashValue) {
        // Get the last two bytes of the MD5 Hash value
        var checksum = String.format("%02x%02x", hashValue[hashValue.length - 2],
                hashValue[hashValue.length - 1]);

        if (checksum.isEmpty()) throw new IllegalArgumentException("Invalid registration code");
        if (checksum.length() > 4) throw new IllegalArgumentException("Checksum length cannot exceed 4 characters");

        // At this point I am sure the length is valid
        return checksum;
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

    public static String generateRegistrationCode(String username) {
        var usernameInHex = String.format("%08x", username.hashCode());
        var code = generateHexadecimalCode(8);

        return usernameInHex + code;
    }
}
