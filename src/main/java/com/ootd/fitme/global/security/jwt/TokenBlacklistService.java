package com.ootd.fitme.global.security.jwt;

import java.time.Instant;
import java.util.UUID;

public interface TokenBlacklistService {
    void blacklist(String jti, Instant expiredAt);
    boolean isBlacklisted(String jti);
    void setRevokeAllBefore(UUID userId, Instant cutoff);
    Instant getRevokeAllBefore(UUID userId);
}
