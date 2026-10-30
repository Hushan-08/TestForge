package com.testforge.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for hashing and verifying passwords securely using BCrypt with automatic salting.
 */
public class PasswordUtil {

    private static final int BCRYPT_LOG_ROUNDS = 12;

    /**
     * Hashes a plain-text password using BCrypt.
     *
     * @param plainTextPassword The raw password entered by the user
     * @return The hashed password string containing salt and cost factor
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
    }

    /**
     * Verifies whether a plain-text password matches a stored BCrypt hash.
     *
     * @param plainTextPassword The raw password to verify
     * @param hashedPassword    The stored BCrypt hash
     * @return true if the password matches, false otherwise
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (Exception e) {
            System.err.println("Password verification error: " + e.getMessage());
            return false;
        }
    }
}
