package com.ootd.fitme.global.security.jwt;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@Primary
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisTokenBlacklistService implements TokenBlacklistService {
    private static final String BLACKLIST_KEY_PATTERN = "fitme:auth:blacklist:%s";
    private static final String REVOKE_BEFORE_KEY_PATTERN = "fitme:auth:revoke-before:%s";
    private static final String BLACKLIST_MARKER = "1";

    private final StringRedisTemplate redisTemplate;
    private final ValueOperations<String, String> valueOps;

    public RedisTokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
    }

    @Override
    public void blacklist(String jti, Instant expiredAt) {
        if (jti == null || jti.isBlank() || expiredAt == null) {
            return;
        }

        String key = blacklistKey(jti);
        Duration ttl = Duration.between(Instant.now(), expiredAt);

        if (ttl.isZero() || ttl.isNegative()) {
            redisTemplate.delete(key);
            return;
        }

        valueOps.set(key, BLACKLIST_MARKER, ttl);
    }

    @Override
    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }

        Boolean exists = redisTemplate.hasKey(blacklistKey(jti));
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void setRevokeAllBefore(UUID userId, Instant cutoff) {
        if (userId == null || cutoff == null) {
            return;
        }

        valueOps.set(revokeBeforeKey(userId), String.valueOf(cutoff.toEpochMilli()));
    }

    @Override
    public Instant getRevokeAllBefore(UUID userId) {
        if (userId == null) {
            return null;
        }

        String key = revokeBeforeKey(userId);
        String raw = valueOps.get(key);

        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return Instant.ofEpochMilli(Long.parseLong(raw));
        } catch (NumberFormatException e) {
            redisTemplate.delete(key);
            return null;
        }
    }

    private String blacklistKey(String jti) {
        return BLACKLIST_KEY_PATTERN.formatted(jti);
    }

    private String revokeBeforeKey(UUID userId) {
        return REVOKE_BEFORE_KEY_PATTERN.formatted(userId);
    }
}
