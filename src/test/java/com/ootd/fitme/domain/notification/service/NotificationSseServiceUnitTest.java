package com.ootd.fitme.domain.notification.service;

import com.ootd.fitme.domain.notification.dto.response.NotificationDto;
import com.ootd.fitme.domain.notification.repository.EmitterRepository;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSseServiceUnitTest {

    @Mock
    private EmitterRepository emitterRepository;

    @Mock
    private SseMessageRepository sseMessageRepository;

    @InjectMocks
    private NotificationSseService notificationSseService;

    @Nested
    @DisplayName("subscribe()")
    class SubscribeTest {

        @Test
        @DisplayName("subscribe 호출 시 emitter가 저장된다")
        void shouldSaveEmitterWhenSubscribe() {
            // given
            UUID userId = UUID.randomUUID();
            UUID lastEventId = UUID.randomUUID();;

            // when
            SseEmitter result = notificationSseService.subscribe(userId,lastEventId,"userAgent");

            // then
            assertThat(result).isNotNull();

            ArgumentCaptor<String> emitterIdCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<SseEmitter> emitterCaptor = ArgumentCaptor.forClass(SseEmitter.class);

            verify(emitterRepository).save(eq(userId), emitterIdCaptor.capture(), emitterCaptor.capture());

            String savedEmitterId = emitterIdCaptor.getValue();
            SseEmitter savedEmitter = emitterCaptor.getValue();

            assertThat(savedEmitterId).isEqualTo("unknown");
            assertThat(savedEmitter).isNotNull();
            assertThat(savedEmitter).isSameAs(result);
        }
    }

    @Nested
    @DisplayName("send()")
    class SendTest {

        @Test
        @DisplayName("send 호출 시 repository에서 emitter를 조회한다")
        void shouldFindEmittersWhenSend() throws IOException {
            // given
            UUID userId = UUID.randomUUID();
            String emitterId = userId + "_12345";

            SseEmitter emitter = mock(SseEmitter.class);
            NotificationDto data = new NotificationDto(
                    UUID.randomUUID(),
                    Instant.now(),
                    userId,
                    "제목",
                    "내용",
                    null
            );
            SseMessage message = SseMessage.create(userId, data);
            given(sseMessageRepository.save(any(SseMessage.class))).willReturn(message);

            given(emitterRepository.findAllByUserId(userId))
                    .willReturn(Map.of(emitterId, emitter));

            // when
            notificationSseService.send(userId, data);

            // then
            verify(emitterRepository).findAllByUserId(userId);
            verify(emitter).send(any(Set.class));
        }

        @Test
        @DisplayName("send 중 IllegalStateException 발생 시 emitter를 삭제한다")
        void shouldDeleteEmitterWhenIOExceptionOccurs() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            String emitterId = userId + "_12345";

            SseEmitter emitter = mock(SseEmitter.class);
            NotificationDto data = new NotificationDto(
                    UUID.randomUUID(),
                    Instant.now(),
                    userId,
                    "제목",
                    "내용",
                    null
            );
            SseMessage message = SseMessage.create(userId, data);
            given(sseMessageRepository.save(any(SseMessage.class))).willReturn(message);

            given(emitterRepository.findAllByUserId(userId))
                    .willReturn(Map.of(emitterId, emitter));

            doThrow(new IllegalStateException("already completed"))
                    .when(emitter)
                    .send(any(Set.class));

            // when
            notificationSseService.send(userId, data);

            // then
            verify(emitterRepository).findAllByUserId(userId);
            verify(emitterRepository).deleteByUserIdAndEmitterId(userId, emitterId);
        }

        @Test
        @DisplayName("send 중 IllegalStateException 발생 시 emitter를 삭제한다")
        void shouldDeleteEmitterWhenIllegalStateExceptionOccurs() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            String emitterId = userId + "_12345";

            SseEmitter emitter = mock(SseEmitter.class);

            NotificationDto data = new NotificationDto(
                    UUID.randomUUID(),
                    Instant.now(),
                    userId,
                    "제목",
                    "내용",
                    null
            );

            SseMessage message = SseMessage.create(userId, data);
            given(sseMessageRepository.save(any(SseMessage.class))).willReturn(message);

            given(emitterRepository.findAllByUserId(userId))
                    .willReturn(Map.of(emitterId, emitter));

            doThrow(new IllegalStateException("already completed"))
                    .when(emitter)
                    .send(any(Set.class));

            // when
            notificationSseService.send(userId, data);

            // then
            verify(emitterRepository).findAllByUserId(userId);
            verify(emitterRepository).deleteByUserIdAndEmitterId(userId, emitterId);
        }
    }
}