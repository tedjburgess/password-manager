
package org.example;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;

import de.mkammerer.argon2.Argon2Advanced;
import de.mkammerer.argon2.Argon2Factory;

public class CryptoUtils {
    public record EncryptedVaultKey(byte[] ciphertext, byte[] nonce) {}

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

    private static SecretKey createVaultEncryptionKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();
        return secretKey;
    }

    private static EncryptedVaultKey encryptVaultEncryptionKey(byte[] unlockingKey, SecretKey vaultKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SecretKey aesKey = new SecretKeySpec(unlockingKey, "AES");  
        byte[] nonce = new byte[12]; //12 random bytes
        new SecureRandom().nextBytes(nonce); //Random number generator, fills with 12 random bytes
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, nonce); //AES-GCM settings
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, spec);
        byte[] encryptedVaultKey = cipher.doFinal(vaultKey.getEncoded());
        return new EncryptedVaultKey(encryptedVaultKey, nonce);
              
    }

    public static EncryptedVaultKey generateSecureEncryptedVaultKey(char[] masterPassword, byte[] salt) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] unlockingKey = deriveUnlockingKey(masterPassword, salt);
        SecretKey vaultKey = createVaultEncryptionKey();
        return encryptVaultEncryptionKey(unlockingKey, vaultKey);
   
    }
}

