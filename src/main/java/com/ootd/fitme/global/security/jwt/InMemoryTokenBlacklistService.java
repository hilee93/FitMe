package com.ootd.fitme.global.security.jwt;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryTokenBlacklistService implements TokenBlacklistService {
    private final ConcurrentHashMap<String, Instant> blacklisted = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Instant> revokeAllBefore = new ConcurrentHashMap<>();

    @Override
    public void blacklist(String jti, Instant expiredAt) {
        blacklisted.put(jti, expiredAt);
    }

    @Override
    public boolean isBlacklisted(String jti) {
        Instant expiresAt = blacklisted.get(jti);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isBefore(Instant.now())) {
            blacklisted.remove(jti);
            return false;
        }
        return true;
    }

    @Override
    public void setRevokeAllBefore(UUID userId, Instant cutoff) {
        revokeAllBefore.put(userId, cutoff);
    }

    @Override
    public Instant getRevokeAllBefore(UUID userId) {
        return revokeAllBefore.get(userId);
    }

    // TODO(Redis): RedisTokenBlacklistService로 교체
    // - key: auth:blacklist:{jti} -> TTL = exp-now
    // - key: auth:revoke-before:{userId} -> Instant epoch millis
}
