package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserService {
    String username;
    
    public UserService(String name) {
        this.username = name;
    }

    public long registerUser() {
        byte[] userSalt = CryptoUtils.generateSalt();
        
        String url = "jdbc:sqlite:vault.db";

        String sql = """
                INSERT INTO users (username, salt)
                VALUES(?, ?)
                RETURNING id;
                """;

        try (Connection connection = DriverManager.getConnection(url);
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setBytes(2, userSalt);
            

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
