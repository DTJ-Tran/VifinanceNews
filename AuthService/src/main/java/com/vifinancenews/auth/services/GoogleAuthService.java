package com.vifinancenews.auth.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleAuthService {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String CLIENT_ID = dotenv.get("GOOGLE_CLIENT_ID");
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Verifies the Google ID token and retrieves the email if the token is valid.
     * 
     * @param idTokenString The ID token to verify.
     * @return The email from the ID token if valid, or null if invalid.
     */
    public static String getEmailFromIdToken(String idTokenString) {
        try {
            // Create the GoogleIdTokenVerifier with the client ID for validation.
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY
            )
            .setAudience(Collections.singletonList(CLIENT_ID)) // Ensure the token is meant for our client.
            .build();

            // Verify the token and extract the payload.
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                Payload payload = idToken.getPayload();
                return payload.getEmail(); // Return the email if the token is valid.
            } else {
                // Token verification failed.
                System.err.println("Invalid ID token.");
                return null;
            }
        } catch (GeneralSecurityException | IOException e) {
            // Log any errors that occur during token verification.
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verifies the Google ID token and retrieves the user's name and email if the token is valid.
     *
     * @param idTokenString The ID token to verify.
     * @return A UserDetails object containing the name and email, or null if invalid.
     */
    public static UserDetails getUserDetailsFromIdToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY
            )
            .setAudience(Collections.singletonList(CLIENT_ID))
            .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                // You can extend this to get other details like picture, etc.

                // Return a custom UserDetails object with the email and name
                return new UserDetails(email, name);
            } else {
                System.err.println("Invalid ID token.");
                return null;
            }
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Custom UserDetails class to hold user information.
    public static class UserDetails {
        private String email;
        private String name;

        public UserDetails(String email, String name) {
            this.email = email;
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }
    }
}
