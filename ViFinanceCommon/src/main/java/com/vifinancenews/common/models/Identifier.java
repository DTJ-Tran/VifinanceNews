package com.vifinancenews.common.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Identifier {
    private UUID id;
    private String email;
    private String passwordHash;
    private String loginMethod; 
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private int failedAttempts;
    private LocalDateTime lockoutUntil;

    public Identifier(UUID id, String email, String passwordHash, String loginMethod,
                      LocalDateTime createdAt, LocalDateTime lastLogin,
                      int failedAttempts, LocalDateTime lockoutUntil) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.loginMethod = loginMethod;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.failedAttempts = failedAttempts;
        this.lockoutUntil = lockoutUntil;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getLoginMethod() { return loginMethod; }
    public void setLoginMethod(String loginMethod) { this.loginMethod = loginMethod; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public LocalDateTime getLockoutUntil() { return lockoutUntil; }
    public void setLockoutUntil(LocalDateTime lockoutUntil) { this.lockoutUntil = lockoutUntil; }

    // Account lock logic
    public void incrementFailedAttempts() { this.failedAttempts++; }
    public void resetFailedAttempts() { this.failedAttempts = 0; }

    public void lockAccount(LocalDateTime until) { this.lockoutUntil = until; }

    public boolean isLocked() {
        if (lockoutUntil != null && lockoutUntil.isAfter(LocalDateTime.now())) {
            return true;
        }
        if (lockoutUntil != null && lockoutUntil.isBefore(LocalDateTime.now())) {
            resetFailedAttempts();
            lockoutUntil = null;
        }
        return false;
    }

    public void resetFailedAttemptsAfterTimeout(LocalDateTime lastLoginTime, int lockoutMinutes) {
        if (lastLoginTime != null && lastLoginTime.plusMinutes(lockoutMinutes).isBefore(LocalDateTime.now())) {
            resetFailedAttempts();
        }
    }
}
