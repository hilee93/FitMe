package com.ootd.fitme.domain.user.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface TemporaryPasswordStore {
    void save(UUID userId, String encodedPassword, Instant expiresAt);
    Optional<String> findValidEncodedPassword(UUID userId);
    void delete(UUID userId);
}
