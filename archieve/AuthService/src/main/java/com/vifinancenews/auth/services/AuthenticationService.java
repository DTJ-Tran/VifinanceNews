package com.vifinancenews.auth.services;

import com.vifinancenews.common.daos.AccountDAO;
import com.vifinancenews.common.daos.IdentifierDAO;
import com.vifinancenews.common.models.Account;
import com.vifinancenews.common.models.Identifier;
import com.vifinancenews.common.utilities.EmailUtility;
import com.vifinancenews.common.utilities.IDHash;
import com.vifinancenews.common.utilities.OTPGenerator;
import com.vifinancenews.common.utilities.PasswordHash;
import com.vifinancenews.common.utilities.RedisCacheService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class AuthenticationService {

    public static record LoginResult(String userId, boolean softDeleted, boolean expired) {}
    
    // ========== Registration ==========
    public boolean registerUser(String email, String password, String userName, String avatarLink, String bio, String loginMethod) throws SQLException {
        String passwordHash = loginMethod.equals("local") ? PasswordHash.hashPassword(password) : null;
        Identifier newIdentifier = IdentifierDAO.insertIdentifier(email, passwordHash, loginMethod);
        if (newIdentifier == null) return false;

        Account newAccount = AccountDAO.insertAccount(newIdentifier.getId(), userName, avatarLink, bio);
        return newAccount != null;
    }

    public boolean createUserFromGoogle(String email, String userName, String avatarLink, String bio) throws SQLException {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank for Google login.");
        }
    
        String generatedUserName = (userName != null && !userName.isBlank())
                ? userName
                : "google_user_" + UUID.randomUUID().toString().substring(0, 8);
    
        Identifier newIdentifier = IdentifierDAO.insertIdentifier(email, null, "google");
        if (newIdentifier == null) return false;
    
        Account newAccount = AccountDAO.insertAccount(newIdentifier.getId(), generatedUserName, avatarLink, bio);
        return newAccount != null;
    }
    

    // ========== Local Login ==========
    public boolean verifyPassword(String email, String password) throws SQLException {
        Identifier user = IdentifierDAO.getIdentifierByEmail(email);
        if (user == null || !user.getLoginMethod().equalsIgnoreCase("local")) return false;
        if (isAccountLocked(user)) return false;

        boolean passwordMatches = PasswordHash.verifyPassword(password, user.getPasswordHash());
        if (!passwordMatches) {
            handleFailedLoginAttempt(user);
            return false;
        }

        sendOTP(email);
        return true;
    }

    public LoginResult login(String email, String enteredOTP) throws SQLException {
        Identifier user = IdentifierDAO.getIdentifierByEmail(email);
        if (user == null || !user.getLoginMethod().equalsIgnoreCase("local")) return null;
    
        if (!verifyOTP(email, enteredOTP)) return null;
    
        IdentifierDAO.resetFailedAttempts(email);
    
        boolean softDeleted = isAccountSoftDeleted(user.getId());
        boolean expired = false;
    
        if (softDeleted && !isWithinReactivationPeriod(user.getId())) {
            expired = true;
        }
    
        // Cache user data after login 
        Account account = AccountDAO.getAccountByUserId(user.getId());
        if (account != null) {
    
            // Create a Map<String, String> for user data
            Map<String, String> userData = new HashMap<>();
            userData.put("userName", account.getUserName());
    
            // Handle potential null values for avatarLink and bio
            userData.put("avatarLink", account.getAvatarLink() != null ? account.getAvatarLink() : "");
            userData.put("bio", account.getBio() != null ? account.getBio() : "");
    
            // Cache the user data with the hashed userId
            RedisCacheService.cacheUserData(account.getUserId(), userData);
        }

        
        
        return new LoginResult(user.getId().toString(), softDeleted, expired);
    }
    
    

    // ========== Google Login ==========
    public String loginWithGoogle(String email) throws SQLException {
        Identifier user = IdentifierDAO.getIdentifierByEmail(email);
        if (user == null || !user.getLoginMethod().equalsIgnoreCase("google")) return null;

        IdentifierDAO.resetFailedAttempts(email);
        return user.getId().toString();
    }

    // ========== Internal Utilities ==========
    private boolean isAccountLocked(Identifier user) {
        if (user.isLocked()) {
            System.out.println("Account locked until: " + user.getLockoutUntil());
            return true;
        }
        return false;
    }

    private void handleFailedLoginAttempt(Identifier user) throws SQLException {
        int newFailedAttempts = user.getFailedAttempts() + 1;
        LocalDateTime lockoutUntil = newFailedAttempts >= 5 ? LocalDateTime.now().plusMinutes(15) : null;
        IdentifierDAO.updateFailedAttempts(user.getEmail(), newFailedAttempts, lockoutUntil);
    }

    private void sendOTP(String email) {
        String otp = OTPGenerator.generateOTP();
        RedisCacheService.storeOTP(email, otp);
        System.out.println("Generated OTP: " + otp);

        try {
            EmailUtility.sendOTP(email, otp);
            System.out.println("OTP sent to " + email);
        } catch (Exception e) {
            System.out.println("Error sending OTP: " + e.getMessage());
        }
    }

    private boolean verifyOTP(String email, String enteredOTP) throws SQLException {
        if (!RedisCacheService.verifyOTP(email, enteredOTP)) {
            System.out.println("Invalid or expired OTP.");
            return false;
        }

        IdentifierDAO.resetFailedAttempts(email);
        return true;
    }

    // ========== Public Utils ==========
    public boolean emailExists(String email) throws SQLException {
        return IdentifierDAO.getIdentifierByEmail(email) != null;
    }

    public Identifier findByEmail(String email) throws SQLException {
        return IdentifierDAO.getIdentifierByEmail(email);
    }

    public boolean isAccountSoftDeleted(UUID userId) throws SQLException {
        return AccountDAO.isAccountInDeleted(userId);
    }

    public boolean isWithinReactivationPeriod(UUID userId) throws SQLException {
        Optional<LocalDateTime> deletedAt = AccountDAO.getDeletedAccountDeletedAt(userId);
        if (deletedAt.isPresent()) {
            LocalDateTime reactivationDeadline = deletedAt.get().plusDays(30);
            return LocalDateTime.now().isBefore(reactivationDeadline);
        }
        return false;
    }

    public boolean restoreUser(UUID userId) throws SQLException {
        return isWithinReactivationPeriod(userId) && AccountDAO.restoreUserById(userId);
    }
}
