package com.vifinancenews.common.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.HashMap;
import java.util.Map;

public class RedisCacheService {
    private static final int OTP_EXPIRY_SECONDS = 300; // 5 minutes
    private static final int USER_DATA_EXPIRY_SECONDS = 3600; // 1 hour
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // === OTP METHODS ===

    public static void storeOTP(String email, String otp) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            String key = formatOtpKey(email);
            jedis.setex(key, OTP_EXPIRY_SECONDS, otp);
            System.out.println("OTP stored in Redis for: " + email);
        } catch (JedisException e) {
            System.err.println("Redis error while storing OTP: " + e.getMessage());
        }
    }

    public static boolean verifyOTP(String email, String inputOTP) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            String key = formatOtpKey(email);
            String storedOTP = jedis.get(key);
            if (storedOTP == null) {
                System.out.println("OTP expired or not found for: " + email);
                return false;
            }

            if (storedOTP.equals(inputOTP)) {
                jedis.del(key);
                System.out.println("OTP verified and deleted for: " + email);
                return true;
            }

            System.out.println("Invalid OTP for: " + email);
            return false;
        } catch (JedisException e) {
            System.err.println("Redis error while verifying OTP: " + e.getMessage());
            return false;
        }
    }

    public static void clearOTP(String email) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            jedis.del(formatOtpKey(email));
        }
    }

    // === USER DATA METHODS ===

    public static void cacheUserData(String userId, Map<String, String> userData) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            String key = formatUserKey(userId);
    
            // Handle empty fields by replacing null with an empty string
            Map<String, String> cleanedData = new HashMap<>();
            for (Map.Entry<String, String> entry : userData.entrySet()) {
                String value = entry.getValue();
                
                // Replace null values with empty strings for avatarLink and bio
                if (value == null) {
                    if (entry.getKey().equals("avatarLink") || entry.getKey().equals("bio")) {
                        cleanedData.put(entry.getKey(), ""); // Replace with empty string
                    }
                } else {
                    cleanedData.put(entry.getKey(), value); // Keep the non-null value
                }
            }
    
            // Store each field as a Redis hash
            jedis.hset(key, cleanedData);
            jedis.expire(key, USER_DATA_EXPIRY_SECONDS);
    
            System.out.println("User data successfully cached for: " + userId);
        } catch (Exception e) {
            System.err.println("Error while caching user data for userId: " + userId + ", data: " + userData);
            e.printStackTrace();
        }
    }
    
    
    
    public static Map<String, String> getCachedUserData(String userId) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            String key = formatUserKey(userId);
            Map<String, String> userData = jedis.hgetAll(key);
    
            if (userData.isEmpty()) {
                System.out.println("No cached user data found for userId: " + userId);
                return null;
            }
    
            return userData;
        } catch (Exception e) {
            System.err.println("Error while retrieving user data for userId: " + userId);
            e.printStackTrace();
            return null;
        }
    }
    

    public static void clearUserData(String userId) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            jedis.del(formatUserKey(userId));
        }
    }

    // === KEY HELPERS ===

    private static String formatOtpKey(String email) {
        return "otp:" + email.replace("@", "_").replace(".", "_");
    }

    private static String formatUserKey(String userId) {
        return "user:" + userId;
    }
}
