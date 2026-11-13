package org.example;

import java.security.SecureRandom;

public class CryptoUtils {
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; //Fills initially with 16 zeros
        random.nextBytes(salt); //Fills with random bytes
        return salt;
    }
}
