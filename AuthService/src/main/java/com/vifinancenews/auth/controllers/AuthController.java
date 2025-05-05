package com.vifinancenews.auth.controllers;

import com.vifinancenews.auth.services.AuthenticationService;
import com.vifinancenews.common.utilities.RedisSessionManager;
import io.javalin.http.Handler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthController {
    private static final AuthenticationService authService = new AuthenticationService();

    public static Handler register = ctx -> {
        try {
            System.out.println("Incoming registration: " + ctx.body());
            Map<String, String> requestBody = ctx.bodyAsClass(Map.class);

            String email = requestBody.get("email");
            String password = requestBody.get("password");
            String userName = requestBody.get("userName");
            String avatarLink = requestBody.get("avatarLink");
            String bio = requestBody.get("bio");
            String loginMethod = requestBody.getOrDefault("loginMethod", "local");

            if (email == null || userName == null || loginMethod == null ||
                    (loginMethod.equals("local") && password == null)) {
                ctx.status(400).json(Map.of("error", "Missing required fields"));
                return;
            }

            boolean success = authService.registerUser(email, password, userName, avatarLink, bio, loginMethod);

            if (success) {
                ctx.status(201).json(Map.of("message", "Registration successful"));
            } else {
                ctx.status(400).json(Map.of("error", "Registration failed"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Internal server error", "details", e.getMessage()));
        }
    };

    public static Handler verifyCredentials = ctx -> {
        try {
            Map<String, String> requestBody = ctx.bodyAsClass(Map.class);
            String email = requestBody.get("email");
            String password = requestBody.get("password");

            boolean isVerified = authService.verifyPassword(email, password);
            if (isVerified) {
                ctx.status(200).json(Map.of("message", "OTP sent (or not required for Google login)"));
            } else {
                ctx.status(401).json(Map.of("error", "Invalid email or password"));
            }
        } catch (Exception e) {
            e.printStackTrace(); // OK for dev, but log properly in prod
            ctx.status(500).json(Map.of(
                "error", "Server error",
                "details", e.getMessage() != null ? e.getMessage() : "Unknown error"
            ));
        }
    };

    public static Handler login = ctx -> {
        Map<String, String> requestBody = ctx.bodyAsClass(Map.class);
        String email = requestBody.get("email");
        String otp = requestBody.get("otp");

        AuthenticationService.LoginResult result = authService.login(email, otp);

        if (result == null) {
            ctx.status(401).json(Map.of("message", "Invalid OTP or login failed"));
            return;
        }

        if (result.expired()) {
            ctx.status(403).json(Map.of("message", "Account permanently deleted. Reactivation not possible."));
            return;
        }

        // Create session with userId stored as string
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("userId", result.userId()); // safe serialization
        String sessionId = RedisSessionManager.createSession(sessionData);

        // Store session ID in cookie
        ctx.cookie("SESSION_ID", sessionId, 3600);

        if (result.softDeleted()) {
            ctx.status(200).json(Map.of(
                    "message", "Account is in reactivation period",
                    "actionRequired", "reactivate",
                    "userId", result.userId()
            ));
        } else {
            ctx.status(200).json(Map.of(
                    "message", "Login successful",
                    "userId", result.userId()
            ));
        }
    };

    public static Handler reactivateAccount = ctx -> {
        String sessionId = ctx.cookie("SESSION_ID");
        Map<String, Object> sessionData = RedisSessionManager.getSession(sessionId);

        if (sessionData == null || sessionData.get("userId") == null) {
            ctx.status(401).json(Map.of("error", "Unauthorized"));
            return;
        }

        try {
            UUID userId = UUID.fromString((String) sessionData.get("userId")); // safe casting
            boolean reactivated = authService.restoreUser(userId);

            if (reactivated) {
                ctx.status(200).json(Map.of("message", "Account reactivated successfully"));
            } else {
                ctx.status(400).json(Map.of("error", "Failed to reactivate account"));
            }
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", "Invalid session user ID"));
        }
    };

    public static Handler logout = ctx -> {
        String sessionId = ctx.cookie("SESSION_ID");
        if (sessionId != null) {
            RedisSessionManager.destroySession(sessionId);
            ctx.removeCookie("SESSION_ID");
        }
        ctx.status(200).json(Map.of("message", "Logout successful"));
    };

    public static Handler checkAuth = ctx -> {
        String sessionId = ctx.cookie("SESSION_ID");
        Map<String, Object> sessionData = RedisSessionManager.getSession(sessionId);

        if (sessionData != null && sessionData.get("userId") != null) {
            ctx.json(Map.of("loggedIn", true, "userId", sessionData.get("userId")));
        } else {
            ctx.json(Map.of("loggedIn", false));
        }
    };

    public static Handler requestPasswordReset = ctx -> {
        try {
            Map<String, String> requestBody = ctx.bodyAsClass(Map.class);
            String email = requestBody.get("email");
    
            if (email == null) {
                ctx.status(400).json(Map.of("error", "Email is required"));
                return;
            }
    
            boolean success = authService.requestPasswordReset(email);
            if (success) {
                ctx.status(200).json(Map.of("message", "OTP sent to email"));
            } else {
                ctx.status(404).json(Map.of("error", "User not found or not eligible"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Server error", "details", e.getMessage()));
        }
    };
    
    
    public static Handler resetPassword = ctx -> {
        try {
            Map<String, String> requestBody = ctx.bodyAsClass(Map.class);
            String email = requestBody.get("email");
            String otp = requestBody.get("otp");
            String newPassword = requestBody.get("newPassword");
    
            if (email == null || otp == null || newPassword == null) {
                ctx.status(400).json(Map.of("error", "Missing required fields"));
                return;
            }
    
            boolean success = authService.resetPassword(email, otp, newPassword);
    
            if (success) {
                ctx.status(200).json(Map.of("message", "Password reset successful"));
            } else {
                ctx.status(400).json(Map.of("error", "Password reset failed. Check OTP or email."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Internal server error", "details", e.getMessage()));
        }
    };
    
}
