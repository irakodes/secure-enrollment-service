package online.eracodes.secureenrollmentservice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

public class EncryptionUtil {

    private final static Logger log = LoggerFactory.getLogger(EncryptionUtil.class);

    /**
     * Hashes the registration code generated with MD5 Hash
     *
     * @param input The requested input
     * @return An MD5 hash of the registration code
     */
    public static byte[] getMD5Hash(String input) {
        log.debug("Input to hash: {}", input);
        return DigestUtils.md5Digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isValidMD5Hash(String input, String hash) {
        return new String(getMD5Hash(input)).equals(hash);
    }

    public static boolean isValidMD5Hash(String input, byte[] hash) {
        return new String(getMD5Hash(input)).equals(new String(hash));
    }
}
