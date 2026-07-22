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
public class AccessTokenBlacklistStore {

    private static final String KEY_PREFIX = "auth:blacklist:access:";

    private final RedisTemplate<String, Object> redisTemplate;

    public void blacklist(String accessToken, long ttlMillis) {
        if (ttlMillis > 0) {
            redisTemplate.opsForValue().set(key(accessToken), true, Duration.ofMillis(ttlMillis));
        }
    }

    public boolean contains(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(accessToken)));
    }

    private String key(String accessToken) {
        return KEY_PREFIX + hash(accessToken);
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
