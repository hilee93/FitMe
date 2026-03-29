package com.ootd.fitme.domain.notification.service;

import com.ootd.fitme.domain.notification.dto.response.NotificationDto;
import com.ootd.fitme.domain.notification.sse.EmitterRepository;
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
            emitterRepository.deleteByUserIdAndEmitterId(userId, emitterId);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE timeout userId={}, emitterId={}", userId, emitterId);
            emitterRepository.deleteByUserIdAndEmitterId(userId, emitterId);
        });

        emitter.onError((e) -> {
            log.error("SSE error userId={}, emitterId={}", userId, emitterId, e);
            emitterRepository.deleteByUserIdAndEmitterId(userId, emitterId);
        });

        emitterRepository.save(userId, emitterId, emitter);

        sendToClient(emitter, userId, emitterId, "connect", null);

        return emitter;
    }

    public void send(UUID userId, String eventName, NotificationDto data) {
        Map<String, SseEmitter> emitters = emitterRepository.findAllByUserId(userId);

        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
            String emitterId = entry.getKey();
            SseEmitter emitter = entry.getValue();

            sendToClient(emitter, userId, emitterId, data);
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
            emitterRepository.deleteByUserIdAndEmitterId(userId, emitterId);
        }
    }

    private String createEmitterId(UUID userId) {
        return userId + "_" + System.currentTimeMillis();
    }
}
