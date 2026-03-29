package com.ootd.fitme.domain.notification.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import java.util.Map;
import java.util.UUID;

public interface EmitterRepository {

    SseEmitter save(UUID userId, String emitterId, SseEmitter emitter);

    Map<String, SseEmitter> findAllByUserId(UUID userId);

    Map<UUID, Map<String, SseEmitter>> findAll();

    void deleteByUserId(UUID userId);

    void deleteByUserIdAndEmitterId(UUID userId, String emitterId);
}
