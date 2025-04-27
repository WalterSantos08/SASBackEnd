package com.sas.sas_backend.utils.password;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class HashPassword {
    public static PasswordHashResponse hashPassword(String plainPassword) {
        String salt = BCrypt.gensalt();
        String hash = BCrypt.hashpw(plainPassword, salt);
        return new PasswordHashResponse(hash, salt);
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword, String salt) {
        String hashToCheck = BCrypt.hashpw(plainPassword, salt);
        return hashToCheck.equals(hashedPassword);
    }
}
