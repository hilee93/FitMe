package com.ootd.fitme.domain.notification.service;

import com.ootd.fitme.domain.notification.dto.response.NotificationDto;
import com.ootd.fitme.domain.notification.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSseService {

    // TODO : 이건 방침에따라 달라질것같아서 일단 1시간 넣었다
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private final EmitterRepository emitterRepository;
    private final SseMessageRepository sseMessageRepository;

    public SseEmitter subscribe(UUID userId,UUID lastEventId) {

        emitterRepository.deleteByUserId(userId);
        String emitterId = createEmitterId(userId);

        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

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

        Optional.ofNullable(lastEventId)
                .ifPresentOrElse(
                        id -> {
                            sseMessageRepository.findAllByEventIdAfterAndReceiverId(id, userId)
                                    .forEach(sseMessage -> {
                                        try {
                                            emitter.send(sseMessage.toEvent());
                                        } catch (IOException e) {
                                            log.error(e.getMessage(), e);
                                        }
                                    });
                        },
                        () -> {
                            ping(emitter);
                        }
                );

        return emitter;
    }

    public void send(UUID userId, NotificationDto data) {
        SseMessage message = sseMessageRepository.save(SseMessage.create(userId, data));
        Set<ResponseBodyEmitter.DataWithMediaType> event = message.toEvent();
        Map<String, SseEmitter> emitters = emitterRepository.findAllByUserId(userId);

        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
            String emitterId = entry.getKey();
            SseEmitter emitter = entry.getValue();
            try {
                emitter.send(event);
            } catch (IOException | IllegalStateException e) {
                log.warn("SSE send failed userId={}, emitterId={}", userId, emitterId, e);
                emitterRepository.deleteByUserId(userId);
            }
        }
    }

    private String createEmitterId(UUID userId) {
        return userId + "_" + System.currentTimeMillis();
    }

    private boolean ping(SseEmitter sseEmitter) {
        try {
            sseEmitter.send(SseEmitter.event()
                    .name("ping")
                    .build());
            return true;
        } catch (IOException e) {
            log.error("Failed to send ping event", e);
            return false;
        }
    }

    @Scheduled(fixedDelay = 1000 * 60 * 30)
    public void cleanUp() {
        emitterRepository.findAll().values().stream()
                .flatMap(map -> map.values().stream())
                .filter(emitter -> !ping(emitter))
                .forEach(emitter ->
                        emitter.completeWithError(new RuntimeException("sse ping failed")));
    }

}
