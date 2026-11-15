package org.example;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class CryptoUtilsTest {
    
    @Test 
    void generateSaltReturns16Bytes() {
        byte[] salt = CryptoUtils.generateSalt();
        assertEquals(16, salt.length);
    }

    @Test 
    void deriveUnlockingKeySameKeyTest() {
        byte[] salt = CryptoUtils.generateSalt();
        char[] password = "randomPassword123".toCharArray();
        byte[] firstKey = CryptoUtils.deriveUnlockingKey(password, salt);
        byte[] secondKey = CryptoUtils.deriveUnlockingKey(password, salt);
        assertArrayEquals(firstKey, secondKey);
    }

    @Test 
    void deriveUnlockingKeyDifferingKeyTest() {
        byte[] key1Salt = CryptoUtils.generateSalt();
        byte[] key2Salt = CryptoUtils.generateSalt();
        
        char[] password1 = "randomPassword1".toCharArray();
        char[] password2 = "randomPassword2".toCharArray();

        byte[] key1 = CryptoUtils.deriveUnlockingKey(password1, key1Salt);
        byte[] key2 = CryptoUtils.deriveUnlockingKey(password2, key2Salt);
        
        assertFalse(Arrays.equals(key1, key2));
    }
}
