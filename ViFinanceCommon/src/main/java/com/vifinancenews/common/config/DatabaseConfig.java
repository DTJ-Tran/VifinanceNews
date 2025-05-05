package com.vifinancenews.common.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    private static final Dotenv dotenv = Dotenv.load();

    private static final String HOST = dotenv.get("DB_HOST");
    private static final String PORT = dotenv.get("DB_PORT"); // Default CockroachDB port is 26257
    private static final String DATABASE = dotenv.get("DB_NAME");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    // Build the connection URL for CockroachDB
    private static final String URL = String.format(
            "jdbc:postgresql://%s:%s/%s?sslmode=require", HOST, PORT, DATABASE
    );

    public static Connection getConnection() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user", USER);
        properties.setProperty("password", PASSWORD);
        properties.setProperty("ssl", "true");
        properties.setProperty("sslmode", "require");
    
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(URL, properties);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            return conn;
        } catch (ClassNotFoundException e) {
            throw new SQLException("CockroachDB JDBC driver not found", e);
        } catch (SQLException e) {
            System.err.println("Error while connecting to database: " + e.getMessage());
            throw e; // Rethrow after logging
        }
    }
    
}
