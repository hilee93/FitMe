package com.ootd.fitme.domain.user.service.temppassword;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Primary
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisTemporaryPasswordStore implements TemporaryPasswordStore {
    private static final String KEY_PATTERN = "fitme:auth:temp-password:%s";
    private static final String FIELD_ENCODED_PASSWORD = "encodedPassword";
    private static final String FIELD_EXPIRES_AT_EPOCH_MS = "expiresAtEpochMs";

    private final StringRedisTemplate redisTemplate;
    private final HashOperations<String, String, String> hashOps;

    public RedisTemporaryPasswordStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOps = redisTemplate.opsForHash();
    }

    @Override
    public void save(UUID userId, String encodedPassword, Instant expiresAt) {
        String key = key(userId);

        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isZero() || ttl.isNegative()) {
            redisTemplate.delete(key);
            return;
        }

        hashOps.put(key, FIELD_ENCODED_PASSWORD, encodedPassword);
        hashOps.put(key, FIELD_EXPIRES_AT_EPOCH_MS, String.valueOf(expiresAt.toEpochMilli()));
        redisTemplate.expire(key, ttl);
    }

    @Override
    public Optional<String> findValidEncodedPassword(UUID userId) {
        String key = key(userId);
        Map<String, String> values = hashOps.entries(key);

        if (values == null || values.isEmpty()) {
            return Optional.empty();
        }

        String encodedPassword = values.get(FIELD_ENCODED_PASSWORD);
        String expiresAtRaw = values.get(FIELD_EXPIRES_AT_EPOCH_MS);

        if (encodedPassword == null || expiresAtRaw == null) {
            redisTemplate.delete(key);
            return Optional.empty();
        }

        long expiresAtEpochMs;

        try {
            expiresAtEpochMs = Long.parseLong(expiresAtRaw);
        } catch (NumberFormatException e) {
            redisTemplate.delete(key);
            return Optional.empty();
        }

        if (expiresAtEpochMs <= Instant.now().toEpochMilli()) {
            redisTemplate.delete(key);
            return Optional.empty();
        }

        return Optional.of(encodedPassword);
    }

    @Override
    public void delete(UUID userId) {
        redisTemplate.delete(key(userId));
    }

    private String key(UUID userId) {
        return KEY_PATTERN.formatted(userId);
    }
}
