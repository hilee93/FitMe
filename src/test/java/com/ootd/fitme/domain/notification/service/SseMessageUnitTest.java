package com.ootd.fitme.domain.notification.service;

import com.ootd.fitme.domain.notification.dto.response.NotificationDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SseMessageUnitTest {

    @Test
    @DisplayName("create는 receiverIds, eventName, eventData를 올바르게 생성한다")
    void create() {
        // given
        UUID receiverId = UUID.randomUUID();
        NotificationDto dto = new NotificationDto(
                UUID.randomUUID(),
                Instant.now(),
                receiverId,
                "제목",
                "내용",
                null
        );

        // when
        SseMessage message = SseMessage.create(receiverId, dto);

        // then
        assertThat(message.getEventId()).isNotNull();
        assertThat(message.getReceiverIds()).isEqualTo(receiverId);
        assertThat(message.getEventName()).isEqualTo("notifications");
        assertThat(message.getEventData()).isEqualTo(dto);
    }

    @Test
    @DisplayName("isReceivable은 같은 receiverIds면 true를 반환한다")
    void isReceivableTrue() {
        // given
        UUID receiverId = UUID.randomUUID();
        NotificationDto dto = new NotificationDto(
                UUID.randomUUID(),
                Instant.now(),
                receiverId,
                "제목",
                "내용",
                null
        );
        SseMessage message = SseMessage.create(receiverId, dto);

        // when
        boolean result = message.isReceivable(receiverId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isReceivable은 다른 userId면 false를 반환한다")
    void isReceivableFalse() {
        // given
        UUID receiverId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        NotificationDto dto = new NotificationDto(
                UUID.randomUUID(),
                Instant.now(),
                receiverId,
                "제목",
                "내용",
                null
        );
        SseMessage message = SseMessage.create(receiverId, dto);

        // when
        boolean result = message.isReceivable(otherUserId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("toEvent는 SSE 전송용 이벤트 객체를 생성한다")
    void toEvent() {
        // given
        UUID receiverId = UUID.randomUUID();
        NotificationDto dto = new NotificationDto(
                UUID.randomUUID(),
                Instant.now(),
                receiverId,
                "제목",
                "내용",
                null
        );
        SseMessage message = SseMessage.create(receiverId, dto);

        // when
        Set<ResponseBodyEmitter.DataWithMediaType> event = message.toEvent();

        // then
        assertThat(event).isNotNull();
        assertThat(event).isNotEmpty();
    }
}