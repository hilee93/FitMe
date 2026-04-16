package com.ootd.fitme.domain.notification.service;

import com.ootd.fitme.domain.notification.dto.response.NotificationDto;
import org.junit.jupiter.api.Test;


import org.junit.jupiter.api.DisplayName;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SseMessageRepositoryTest {

    @Test
    @DisplayName("save 후 eventId 이후의 메시지를 receiverId 기준으로 조회할 수 있다")
    void findAllByEventIdAfterAndReceiverId() {
        // given
        SseMessageRepository repository = new SseMessageRepository(10);

        UUID receiverId = UUID.randomUUID();
        UUID otherReceiverId = UUID.randomUUID();

        NotificationDto dto1 = new NotificationDto(
                UUID.randomUUID(), Instant.now(), receiverId, "제목1", "내용1", null
        );
        NotificationDto dto2 = new NotificationDto(
                UUID.randomUUID(), Instant.now(), receiverId, "제목2", "내용2", null
        );
        NotificationDto dto3 = new NotificationDto(
                UUID.randomUUID(), Instant.now(), otherReceiverId, "제목3", "내용3", null
        );
        NotificationDto dto4 = new NotificationDto(
                UUID.randomUUID(), Instant.now(), receiverId, "제목4", "내용4", null
        );

        SseMessage message1 = SseMessage.create(receiverId, dto1);
        SseMessage message2 = SseMessage.create(receiverId, dto2);
        SseMessage message3 = SseMessage.create(otherReceiverId, dto3);
        SseMessage message4 = SseMessage.create(receiverId, dto4);

        repository.save(message1);
        repository.save(message2);
        repository.save(message3);
        repository.save(message4);

        // when
        List<SseMessage> result =
                repository.findAllByEventIdAfterAndReceiverId(message1.getEventId(), receiverId);

        // then
        assertThat(result).containsExactly(message2, message4);
    }

    @Test
    @DisplayName("기준 eventId 이후의 메시지가 없으면 빈 리스트를 반환한다")
    void returnEmptyListWhenNoMessagesAfterEventId() {
        // given
        SseMessageRepository repository = new SseMessageRepository(10);
        UUID receiverId = UUID.randomUUID();

        NotificationDto dto1 = new NotificationDto(
                UUID.randomUUID(), Instant.now(), receiverId, "제목1", "내용1", null
        );
        SseMessage message1 = SseMessage.create(receiverId, dto1);

        repository.save(message1);

        // when
        List<SseMessage> result =
                repository.findAllByEventIdAfterAndReceiverId(message1.getEventId(), receiverId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("저장 용량을 초과하면 가장 오래된 메시지부터 제거된다")
    void removeOldestMessageWhenCapacityExceeded() {
        // given
        SseMessageRepository repository = new SseMessageRepository(3);
        UUID receiverId = UUID.randomUUID();

        SseMessage message1 = SseMessage.create(receiverId,
                new NotificationDto(UUID.randomUUID(), Instant.now(), receiverId, "1", "1", null));
        SseMessage message2 = SseMessage.create(receiverId,
                new NotificationDto(UUID.randomUUID(), Instant.now(), receiverId, "2", "2", null));
        SseMessage message3 = SseMessage.create(receiverId,
                new NotificationDto(UUID.randomUUID(), Instant.now(), receiverId, "3", "3", null));
        SseMessage message4 = SseMessage.create(receiverId,
                new NotificationDto(UUID.randomUUID(), Instant.now(), receiverId, "4", "4", null));

        repository.save(message1);
        repository.save(message2);
        repository.save(message3);
        repository.save(message4);

        // when
        List<SseMessage> result =
                repository.findAllByEventIdAfterAndReceiverId(message1.getEventId(), receiverId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("용량 초과 후에도 남아있는 eventId 기준으로 이후 메시지를 정상 조회한다")
    void findMessagesAfterRemainingEventIdWhenCapacityExceeded() {
        // given
        SseMessageRepository repository = new SseMessageRepository(3);
        UUID receiverId = UUID.randomUUID();

        SseMessage message1 = SseMessage.create(receiverId,
                new NotificationDto(UUID.randomUUID(), Instant.now(), receiverId, "1", "1", null));
        SseMessage message2 = SseMessage.create(receiverId,
                new NotificationDto(UUID.randomUUID(), Instant.now(), receiverId, "2", "2", null));
        SseMessage message3 = SseMessage.create(receiverId,
                new NotificationDto(UUID.randomUUID(), Instant.now(), receiverId, "3", "3", null));
        SseMessage message4 = SseMessage.create(receiverId,
                new NotificationDto(UUID.randomUUID(), Instant.now(), receiverId, "4", "4", null));

        repository.save(message1);
        repository.save(message2);
        repository.save(message3);
        repository.save(message4);

        // when
        List<SseMessage> result =
                repository.findAllByEventIdAfterAndReceiverId(message2.getEventId(), receiverId);

        // then
        assertThat(result).containsExactly(message3, message4);
    }
}