
package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    public static void initialize() {

        String url = "jdbc:sqlite:vault.db";

        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY,
                    username TEXT NOT NULL UNIQUE,
                    salt BLOB NOT NULL UNIQUE,
                    encrypted_vault_key BLOB NOT NULL UNIQUE,
                    vault_key_nonce BLOB NOT NULL UNIQUE,
                    argon2_iterations INTEGER NOT NULL,
                    argon2_memory,
                    argon2_parallelism INTEGER NOT NULL
                )
                """;

        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);

            System.out.println("Database ready.");

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}

