package org.example;

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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class UserService {
    String username;
    
    public UserService(String name) {
        this.username = name;
    }

    public long registerUser(char[] masterPassword) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] salt = CryptoUtils.generateSalt();
        
        //vault key
        CryptoUtils.EncryptedVaultKey encryptedVaultKeyObject = CryptoUtils.generateSecureEncryptedVaultKey(masterPassword, salt);
        Arrays.fill(masterPassword, '\0'); //Reset master password to 0's
        byte[] ciphertext = encryptedVaultKeyObject.ciphertext();
        byte[] nonce = encryptedVaultKeyObject.nonce();
        

        String url = "jdbc:sqlite:vault.db";

        String sql = """
                INSERT INTO users (username, salt, encrypted_vault_key, vault_key_nonce, argon2_iterations, argon2_parallelism)
                VALUES(?, ?, ?, ?, 1, 1)
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
}
