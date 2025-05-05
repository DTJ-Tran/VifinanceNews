package com.vifinancenews.common.utilities;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHash {

    // Hash password using BCrypt
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12)); // 12 rounds of hashing
    }

    // Verify password (compare input with stored hash)
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
