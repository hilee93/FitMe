package com.ootd.fitme.domain.notification.service;

import com.ootd.fitme.domain.notification.dto.response.NotificationDto;
import com.ootd.fitme.domain.notification.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSseService {

    // TODO : 이건 방침에따라 달라질것같아서 일단 1시간 넣었다
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private final EmitterRepository emitterRepository;

    public SseEmitter subscribe(UUID userId) {
        String emitterId = createEmitterId(userId);

        SseEmitter emitter = new SseEmitter();

        emitter.onCompletion(() -> {
            log.debug("SSE completed userId={}, emitterId={}", userId, emitterId);
            emitterRepository.deleteByUserId(userId);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE timeout userId={}, emitterId={}", userId, emitterId);
            emitterRepository.deleteByUserId(userId);
        });

        emitter.onError((e) -> {
            log.error("SSE error userId={}, emitterId={}", userId, emitterId, e);
            emitterRepository.deleteByUserId(userId);
        });

        emitterRepository.save(userId, emitterId, emitter);

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("ping")
                            .data("")
            );
        } catch (IOException | IllegalStateException e) {
            log.warn("SSE ping send failed userId={}, emitterId={}", userId, emitterId, e);
            emitterRepository.deleteByUserId(userId);
        }


        return emitter;
    }

    public void send(UUID userId, NotificationDto data) {
        Map<String, SseEmitter> emitters = emitterRepository.findAllByUserId(userId);

        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
            String emitterId = entry.getKey();
            SseEmitter emitter = entry.getValue();

            sendToClient(emitter, userId, emitterId, data);
        }
    }

    public void sendAll(NotificationDto data) {
        Map<UUID, Map<String, SseEmitter>> allEmitters = emitterRepository.findAll();

        for (Map.Entry<UUID, Map<String, SseEmitter>> userEntry : allEmitters.entrySet()) {
            UUID userId = userEntry.getKey();
            Map<String, SseEmitter> userEmitters = userEntry.getValue();

            for (Map.Entry<String, SseEmitter> emitterEntry : userEmitters.entrySet()) {
                String emitterId = emitterEntry.getKey();
                SseEmitter emitter = emitterEntry.getValue();

                sendToClient(emitter, userId, emitterId, data);
            }
        }
    }

    private void sendToClient(
            SseEmitter emitter,
            UUID userId,
            String emitterId,
            NotificationDto data
    ) {
        try {
            emitter.send(
                    SseEmitter.event()
                            .name("notifications")
                            .data(data)
            );
        } catch (IOException | IllegalStateException e) {
            log.warn("SSE send failed userId={}, emitterId={}", userId, emitterId, e);
            emitterRepository.deleteByUserId(userId);
        }
    }

    private String createEmitterId(UUID userId) {
        return userId + "_" + System.currentTimeMillis();
    }
}
