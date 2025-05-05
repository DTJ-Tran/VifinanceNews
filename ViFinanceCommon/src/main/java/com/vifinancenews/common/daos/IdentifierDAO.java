package com.vifinancenews.common.daos;

import com.vifinancenews.common.config.DatabaseConfig;
import com.vifinancenews.common.models.Identifier;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class IdentifierDAO {

    public static Identifier getIdentifierByEmail(String email) throws SQLException {
        String query = "SELECT id, email, password_hash, login_method, created_at, last_login, failed_attempts, lockout_until FROM identifier WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Identifier(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("login_method"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("last_login") != null ? rs.getTimestamp("last_login").toLocalDateTime() : null,
                        rs.getInt("failed_attempts"),
                        rs.getTimestamp("lockout_until") != null ? rs.getTimestamp("lockout_until").toLocalDateTime() : null
                    );
                }
            }
        }
        return null;
    }

    public static void updateFailedAttempts(String email, int failedAttempts, LocalDateTime lockoutUntil) throws SQLException {
        String query = "UPDATE identifier SET failed_attempts = ?, lockout_until = ? WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, failedAttempts);
            pstmt.setTimestamp(2, lockoutUntil != null ? Timestamp.valueOf(lockoutUntil) : null);
            pstmt.setString(3, email);
            pstmt.executeUpdate();
        }
    }

    public static void updateLastLogin(String email) throws SQLException {
        String query = "UPDATE identifier SET last_login = ?, failed_attempts = 0 WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, email);
            pstmt.executeUpdate();
        }
    }

    public static void resetFailedAttempts(String email) throws SQLException {
        String query = "UPDATE identifier SET failed_attempts = 0 WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            pstmt.executeUpdate();
        }
    }

    public static Identifier insertIdentifier(String email, String passwordHash, String loginMethod) throws SQLException {
        UUID userId = UUID.randomUUID(); // UUID is generated in Java for now
        LocalDateTime createdAt = LocalDateTime.now();

        String query = "INSERT INTO identifier (id, email, password_hash, login_method, created_at, last_login, failed_attempts, lockout_until) " +
                       "VALUES (?, ?, ?, ?, ?, NULL, 0, NULL)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, userId.toString());
            pstmt.setString(2, email);
            pstmt.setString(3, passwordHash);
            pstmt.setString(4, loginMethod);
            pstmt.setTimestamp(5, Timestamp.valueOf(createdAt));

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                return new Identifier(userId, email, passwordHash, loginMethod, createdAt, null, 0, null);
            }
        }
        return null;
    }

    public static boolean deleteIdentifierByEmail(String email) throws SQLException {
        String query = "DELETE FROM identifier WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public static boolean deleteIdentifierByUserId(UUID identifierId) throws SQLException {
        String query = "DELETE FROM identifier WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setObject(1, identifierId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public static boolean deleteExpiredIdentifiers(int days) throws SQLException {
        String query = "DELETE FROM identifier WHERE id IN (SELECT id FROM deleted_accounts WHERE deleted_at < NOW() - INTERVAL ? DAY)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, days);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        }
    }

    public static boolean updatePassword(String email, String newPasswordHash) throws SQLException {
        String query = "UPDATE identifier SET password_hash = ?, failed_attempts = 0, lockout_until = NULL WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
    
            pstmt.setString(1, newPasswordHash);
            pstmt.setString(2, email);
    
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
}
