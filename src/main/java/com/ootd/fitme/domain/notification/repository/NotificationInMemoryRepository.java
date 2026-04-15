package com.ootd.fitme.domain.notification.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class NotificationInMemoryRepository implements EmitterRepository {


    private final Map<UUID, Map<String, SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter save(UUID userId, String emitterId, SseEmitter emitter) {
        emitters
                .computeIfAbsent(userId, key -> new ConcurrentHashMap<>())
                .put(emitterId, emitter);
        log.debug("save emitterId={}, userId={}", emitterId, emitters.get(userId));
        return emitter;
    }

    @Override
    public Map<String, SseEmitter> findAllByUserId(UUID userId) {
        Map<String, SseEmitter> userEmitters = emitters.get(userId);

        if (userEmitters == null) {
            return Map.of();
        }

        return new HashMap<>(userEmitters);
    }

    @Override
    public Map<UUID, Map<String, SseEmitter>> findAll() {
        return emitters;
    }

    @Override
    public void deleteByUserId(UUID userId) {
        emitters.remove(userId);
    }

    @Override
    public void deleteByUserIdAndEmitterId(UUID userId, String emitterId) {
        Map<String, SseEmitter> userEmitters = emitters.get(userId);

        if (userEmitters == null) {
            return;
        }

        userEmitters.remove(emitterId);

        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }
}
