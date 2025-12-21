package online.eracodes.secureenrollmentservice.util;

import java.util.regex.Pattern;

public final class EmailValidatorUtil {

    private EmailValidatorUtil() {
        // utility class
    }

    // Practical, production-grade email regex
    // - allows +, ., _, %
    // - enforces domain labels
    // - supports long TLDs
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@" +
                    "(?:[A-Za-z0-9-]+\\.)+" +
                    "[A-Za-z]{2,63}$"
    );

    // RFC-based length limits
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_LOCAL_PART_LENGTH = 64;

    public static boolean isValid(String email) {
        if (email == null) { return false; }

        var value = email.trim();

        if (value.isEmpty() || value.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        var atIndex = value.indexOf('@');
        if (atIndex <= 0 || atIndex != value.lastIndexOf('@')) {
            return false;
        }

        // local-part length check
        if (atIndex > MAX_LOCAL_PART_LENGTH) { return false; }

        return EMAIL_PATTERN.matcher(value).matches();
    }

    public static void validateOrThrow(String email) {
        if (!isValid(email)) {
            throw new IllegalArgumentException("Invalid email address format");
        }
    }
}