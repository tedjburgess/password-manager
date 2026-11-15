
package org.example;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;

import de.mkammerer.argon2.Argon2Advanced;
import de.mkammerer.argon2.Argon2Factory;

public class CryptoUtils {

    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];

        random.nextBytes(salt);

        return salt;
    }

    public static byte[] deriveUnlockingKey(char[] masterPassword, byte[] salt) {

        Argon2Advanced argon2 = Argon2Factory.createAdvanced(
                Argon2Factory.Argon2Types.ARGON2id
        );

        byte[] unlockingKey = argon2.pbkdf(
                3,
                65536,
                1,
                masterPassword,
                StandardCharsets.UTF_8,
                salt,
                32
        );

        return unlockingKey;
    }

    public static SecretKey keyGenerator() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();
        return secretKey;
    }
}

