package org.example;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class UserService {
    String username;
    private SecretKey vaultKey;
    
    public UserService(String name) {
        this.username = name;
    }

    public long registerUser(char[] masterPassword) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] salt = CryptoUtils.generateSalt();
        byte[] ciphertext;
        byte[] nonce;
        
        //vault key
        try {
            CryptoUtils.EncryptedVaultKey encryptedVaultKeyObject = CryptoUtils.generateSecureEncryptedVaultKey(masterPassword, salt);
            ciphertext = encryptedVaultKeyObject.ciphertext();
            nonce = encryptedVaultKeyObject.nonce();            
        } finally {
            Arrays.fill(masterPassword, '\0');
        }

        String url = "jdbc:sqlite:vault.db";

        String sql = """
                INSERT INTO users (username, salt, encrypted_vault_key, vault_key_nonce, argon2_iterations, argon2_memory, argon2_parallelism)
                VALUES(?, ?, ?, ?, 3, 65536, 1)
                RETURNING id;
                """;

        try (Connection connection = DriverManager.getConnection(url);
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setBytes(2, salt);
            statement.setBytes(3, ciphertext);
            statement.setBytes(4, nonce);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                long userId = result.getLong("id");
                System.out.println("User with id#: " + userId + " created");
                return userId;
            }

            throw new SQLException("User creation did not return an ID.");
        } catch (SQLException e) {
           throw new IllegalStateException("Failed to register user", e); 
        }
    }

    public boolean signIn(char[] masterPassword) throws GeneralSecurityException {
        String url = "jdbc:sqlite:vault.db";

        String sql = """
                SELECT salt, encrypted_vault_key, vault_key_nonce
                FROM users
                WHERE username = ?
                """;
        vaultKey = null; //ensure key is not still stored from last sign in attempt
        try (Connection connection = DriverManager.getConnection(url);
            
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                byte[] salt = result.getBytes("salt");
                byte[] encryptedVaultKey = result.getBytes("encrypted_vault_key");
                byte[] nonce = result.getBytes("vault_key_nonce");

                //Recreate vault unlocking key
                byte[] unlockingKey = CryptoUtils.deriveUnlockingKey(masterPassword, salt);
                
                //Decrypt stored value
                try {
                    vaultKey = CryptoUtils.decryptVaultKey(unlockingKey, encryptedVaultKey, nonce);
                } catch (AEADBadTagException e) {
                    System.out.println("Invalid password");
                    return false;
                } finally {
                    Arrays.fill(unlockingKey, (byte) 0);
                }
                
                return true;
            } else {
                System.out.println("A user with the entered username does not exist.");
                return false;
            }

            //throw new SQLException("User creation did not return an ID.");
        } catch (SQLException e) {
           throw new IllegalStateException("Failed to login", e); 
        } finally {
            Arrays.fill(masterPassword, '\0');
        }
    }

}
