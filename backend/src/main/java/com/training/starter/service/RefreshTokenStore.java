package com.training.starter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "auth:refresh:";

    private final RedisTemplate<String, Object> redisTemplate;

    public void save(String username, String refreshToken, long ttlMillis) {
        redisTemplate.opsForValue().set(key(username), hash(refreshToken), Duration.ofMillis(ttlMillis));
    }

    public boolean matches(String username, String refreshToken) {
        Object storedHash = redisTemplate.opsForValue().get(key(username));
        return storedHash != null && storedHash.equals(hash(refreshToken));
    }

    public void delete(String username) {
        redisTemplate.delete(key(username));
    }

    private String key(String username) {
        return KEY_PREFIX + username;
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
