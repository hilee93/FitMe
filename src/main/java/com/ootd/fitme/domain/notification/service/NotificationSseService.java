package com.ootd.fitme.domain.notification.service;

import com.ootd.fitme.domain.notification.dto.response.NotificationDto;
import com.ootd.fitme.domain.notification.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
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
    private final UserAgentAnalyzer uaa;

    public SseEmitter subscribe(UUID userId,UUID lastEventId,String userAgent) {

        String emitterId = createEmitterId(userAgent);
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        emitter.onCompletion(() -> {
            log.debug("SSE completed userId={}, emitterId={}", userId, emitterId);
            emitterRepository.deleteByUserIdAndEmitterId(userId, emitterId);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE timeout userId={}, emitterId={}", userId, emitterId);
            emitterRepository.deleteByUserIdAndEmitterId(userId, emitterId);
        });

        emitter.onError((e) -> {
            log.warn("SSE error userId={}, emitterId={}", userId, emitterId);
            emitterRepository.deleteByUserIdAndEmitterId(userId, emitterId);
        });

        emitterRepository.save(userId, emitterId, emitter);
        Optional.ofNullable(lastEventId)
                .ifPresentOrElse(
                        id -> {
                            log.info("SSE reconnect userId={}, emitterId={}, 못 받은 알림 조회", userId, emitterId);
                            List<SseMessage> messages =
                                    sseMessageRepository.findAllByEventIdAfterAndReceiverId(id, userId);

                            if (messages.isEmpty()) {
                                log.info("재전송할 이벤트 없음, ping 전송");
                                ping(emitter);
                                return;
                            }
                            log.info("못 받은 알림 수={}", messages.size());
                            messages.forEach(sseMessage -> {
                                try {
                                    emitter.send(sseMessage.toEvent());
                                    log.info("알림 재전송 성공 = {}", sseMessage.toEvent());
                                } catch (IOException | IllegalStateException e) {
                                    log.error("SSE 재전송 실패 userId={}, eventId={}", userId, sseMessage.getEventId(), e);

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
                log.info("SSE send SUCCESS userId={}, eventId={}", userId, message.getEventId());
            } catch (IOException | IllegalStateException e) {
                log.warn("SSE disconnected userId={}, emitterId={}", userId, emitterId);
                emitterRepository.deleteByUserIdAndEmitterId(userId, emitterId);
            } catch (Exception e) {
                log.error("SSE send unexpected error userId={}, emitterId={}", userId, emitterId, e);
                emitterRepository.deleteByUserIdAndEmitterId(userId, emitterId);
            }
        }
    }

    private boolean ping(SseEmitter sseEmitter) {
        try {
            log.info("PING before send");
            sseEmitter.send(SseEmitter.event()
                    .name("ping")
                    .build());
            log.info("PING after send");
            return true;
        } catch (Exception e) {
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

    private String createEmitterId(String userAgent) {
        if (uaa == null || userAgent == null) {
            return "unknown";
        }
        UserAgent agent = uaa.parse(userAgent);

        String agentName = agent.getValue("AgentName");

        return (agentName == null || agentName.isEmpty())
                ? "unknown"
                : agentName;
    }

}
