package com.vifinancenews;

import com.vifinancenews.auth.controllers.AuthController;

import com.vifinancenews.auth.controllers.GoogleAuthController;
import com.vifinancenews.auth.controllers.GuestController;


import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class Main {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            // CORS and Routing Optimizations
            config.router.contextPath = "/";
            config.router.treatMultipleSlashesAsSingleSlash = true;

            // Enable Brotli & Gzip Compression
            config.http.brotliAndGzipCompression();

            // Static Files (relative to project root)
            config.staticFiles.add(staticFileConfig -> {
                staticFileConfig.hostedPath = "/"; // URL path for static files
                staticFileConfig.directory = "static"; // Ensure `/static` is accessible
                staticFileConfig.location = Location.CLASSPATH;
            });
            
        
            // Request Settings
            config.http.asyncTimeout = 30000; // 30 sec timeout
            config.http.maxRequestSize = 10_000_000L; // 10MB max request size
        }).start(6999);


        app.before(ctx -> {
            // CORS Settings
            ctx.header("Access-Control-Allow-Origin", "*"); // Allow all origins
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Cookie"); // Include 'Cookie' in allowed headers

            // Session settings: Add cookie for session expiration
            ctx.cookie("session_timeout", String.valueOf(System.currentTimeMillis()), 600); // Session timeout in seconds
        });
        
        // **Auth Routes**
        app.post("/api/register", AuthController.register);
        app.post("/api/verify", AuthController.verifyCredentials); 
        app.post("/api/login", AuthController.login); 
        app.post("/api/logout", AuthController.logout);
        app.get("/api/auth-status", AuthController.checkAuth);
        app.post("/api/reactivate-account", AuthController.reactivateAccount);
        app.post("/api/forgot-password", AuthController.requestPasswordReset);
        app.post("/api/reset-password", AuthController.resetPassword);


        // **Guest Routes**
        GuestController.registerRoutes(app);
        app.post("/api/google-login", GoogleAuthController.handleGoogleLogin);

        System.out.println("Server running on http://localhost:6999/index.html");
    }
}
