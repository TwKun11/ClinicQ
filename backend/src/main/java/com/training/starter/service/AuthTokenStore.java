package com.training.starter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthTokenStore {

    private final RedisTemplate<String, Object> redisTemplate;

    public String create(String purpose, Long userId, Duration ttl) {
        String token = UUID.randomUUID() + "-" + UUID.randomUUID();
        redisTemplate.opsForValue().set(key(purpose, token), userId.toString(), ttl);
        return token;
    }

    public Long consume(String purpose, String token) {
        String key = key(purpose, token);
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        redisTemplate.delete(key);
        return Long.valueOf(value.toString());
    }

    private String key(String purpose, String token) {
        return "auth:" + purpose + ":" + token;
    }
}
