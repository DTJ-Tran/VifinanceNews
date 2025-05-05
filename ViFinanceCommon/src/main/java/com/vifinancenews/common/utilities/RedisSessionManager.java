package com.vifinancenews.common.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.UUID;

public class RedisSessionManager {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int SESSION_TTL = 3600; // 1 hour

    private static String sessionKey(String sessionId) {
        return "session:" + sessionId;
    }

    public static String createSession(Map<String, Object> sessionData) {
        try (Jedis jedis = com.vifinancenews.common.utilities.RedisConnection.getConnection()) {
            String sessionId = UUID.randomUUID().toString();
            String json = objectMapper.writeValueAsString(sessionData);
            jedis.setex(sessionKey(sessionId), SESSION_TTL, json);
            return sessionId;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, Object> getSession(String sessionId) {
        try (Jedis jedis = com.vifinancenews.common.utilities.RedisConnection.getConnection()) {
            String json = jedis.get(sessionKey(sessionId));
            if (json == null) return null;
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void updateSession(String sessionId, Map<String, Object> sessionData) {
        try (Jedis jedis = com.vifinancenews.common.utilities.RedisConnection.getConnection()) {
            String json = objectMapper.writeValueAsString(sessionData);
            jedis.setex(sessionKey(sessionId), SESSION_TTL, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void destroySession(String sessionId) {
        try (Jedis jedis = com.vifinancenews.common.utilities.RedisConnection.getConnection()) {
            jedis.del(sessionKey(sessionId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
