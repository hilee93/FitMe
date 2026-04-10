package com.ootd.fitme.domain.user.service.temppassword;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class InMemoryTemporaryPasswordStore implements TemporaryPasswordStore {
    private final ConcurrentMap<UUID, TempPasswordEntry> store = new ConcurrentHashMap<>();

    @Override
    public void save(UUID userId, String encodedPassword, Instant expiresAt) {
        store.put(userId, new TempPasswordEntry(encodedPassword, expiresAt));
    }

    @Override
    public Optional<String> findValidEncodedPassword(UUID userId) {
        TempPasswordEntry entry = store.get(userId);
        if (entry == null) {
            return Optional.empty();
        }

        if (entry.expiresAt().isBefore(Instant.now())) {
            store.remove(userId);
            return Optional.empty();
        }

        return Optional.of(entry.encodedPassword());
    }

    @Override
    public void delete(UUID userId) {
        store.remove(userId);
    }

    // TODO: redis 구현체로 교체
}
