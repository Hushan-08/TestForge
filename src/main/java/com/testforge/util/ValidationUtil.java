package com.testforge.util;

import java.util.regex.Pattern;

/**
 * Validates form inputs, email formats, and exam values.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isNonEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidUsername(String username) {
        if (username == null) return false;
        String trimmed = username.trim();
        return trimmed.length() >= 3 && trimmed.length() <= 30 && trimmed.matches("^[a-zA-Z0-9_.]+$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isPositiveInteger(String numberStr) {
        if (numberStr == null || numberStr.trim().isEmpty()) return false;
        try {
            int val = Integer.parseInt(numberStr.trim());
            return val > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
