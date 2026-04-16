package com.ootd.fitme.domain.notification.repository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationInMemoryRepositoryTest {

    @Test
    @DisplayName("save는 userId가 없으면 생성 후 emitter를 저장한다")
    void save() {
        // given
        NotificationInMemoryRepository repository = new NotificationInMemoryRepository();
        UUID userId = UUID.randomUUID();
        String emitterId = "emitter-1";
        SseEmitter emitter = new SseEmitter();

        // when
        repository.save(userId, emitterId, emitter);

        // then
        Map<String, SseEmitter> result = repository.findAllByUserId(userId);
        assertThat(result).hasSize(1);
        assertThat(result).containsEntry(emitterId, emitter);
    }

    @Test
    @DisplayName("같은 userId에 여러 emitter를 저장할 수 있다")
    void saveMultipleEmitters() {
        // given
        NotificationInMemoryRepository repository = new NotificationInMemoryRepository();
        UUID userId = UUID.randomUUID();

        SseEmitter emitter1 = new SseEmitter();
        SseEmitter emitter2 = new SseEmitter();

        // when
        repository.save(userId, "emitter-1", emitter1);
        repository.save(userId, "emitter-2", emitter2);

        // then
        Map<String, SseEmitter> result = repository.findAllByUserId(userId);
        assertThat(result).hasSize(2);
        assertThat(result).containsEntry("emitter-1", emitter1);
        assertThat(result).containsEntry("emitter-2", emitter2);
    }

    @Test
    @DisplayName("findAllByUserId는 없는 userId면 빈 맵을 반환한다")
    void findAllByUserIdWhenUserDoesNotExist() {
        // given
        NotificationInMemoryRepository repository = new NotificationInMemoryRepository();
        UUID userId = UUID.randomUUID();

        // when
        Map<String, SseEmitter> result = repository.findAllByUserId(userId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAllByUserId는 내부 맵의 복사본을 반환한다")
    void findAllByUserIdReturnsCopy() {
        // given
        NotificationInMemoryRepository repository = new NotificationInMemoryRepository();
        UUID userId = UUID.randomUUID();
        String emitterId = "emitter-1";
        SseEmitter emitter = new SseEmitter();

        repository.save(userId, emitterId, emitter);

        // when
        Map<String, SseEmitter> result = repository.findAllByUserId(userId);
        result.remove(emitterId);

        // then
        Map<String, SseEmitter> actual = repository.findAllByUserId(userId);
        assertThat(actual).containsEntry(emitterId, emitter);
    }

    @Test
    @DisplayName("findAll은 전체 emitter 저장소를 반환한다")
    void findAll() {
        // given
        NotificationInMemoryRepository repository = new NotificationInMemoryRepository();

        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        repository.save(user1, "emitter-1", new SseEmitter());
        repository.save(user2, "emitter-2", new SseEmitter());

        // when
        Map<UUID, Map<String, SseEmitter>> result = repository.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsKeys(user1, user2);
    }

    @Test
    @DisplayName("deleteByUserId는 해당 userId의 emitter 전체를 삭제한다")
    void deleteByUserId() {
        // given
        NotificationInMemoryRepository repository = new NotificationInMemoryRepository();
        UUID userId = UUID.randomUUID();

        repository.save(userId, "emitter-1", new SseEmitter());
        repository.save(userId, "emitter-2", new SseEmitter());

        // when
        repository.deleteByUserId(userId);

        // then
        Map<String, SseEmitter> result = repository.findAllByUserId(userId);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteByUserIdAndEmitterId는 특정 emitter만 삭제한다")
    void deleteByUserIdAndEmitterId() {
        // given
        NotificationInMemoryRepository repository = new NotificationInMemoryRepository();
        UUID userId = UUID.randomUUID();

        SseEmitter emitter1 = new SseEmitter();
        SseEmitter emitter2 = new SseEmitter();

        repository.save(userId, "emitter-1", emitter1);
        repository.save(userId, "emitter-2", emitter2);

        // when
        repository.deleteByUserIdAndEmitterId(userId, "emitter-1");

        // then
        Map<String, SseEmitter> result = repository.findAllByUserId(userId);
        assertThat(result).hasSize(1);
        assertThat(result).doesNotContainKey("emitter-1");
        assertThat(result).containsEntry("emitter-2", emitter2);
    }

    @Test
    @DisplayName("마지막 emitter 삭제 시 userId도 함께 제거된다")
    void deleteLastEmitterRemovesUserId() {
        // given
        NotificationInMemoryRepository repository = new NotificationInMemoryRepository();
        UUID userId = UUID.randomUUID();

        repository.save(userId, "emitter-1", new SseEmitter());

        // when
        repository.deleteByUserIdAndEmitterId(userId, "emitter-1");

        // then
        assertThat(repository.findAll()).doesNotContainKey(userId);
    }

    @Test
    @DisplayName("없는 userId에서 emitter 삭제를 시도해도 예외가 발생하지 않는다")
    void deleteByUserIdAndEmitterIdWhenUserDoesNotExist() {
        // given
        NotificationInMemoryRepository repository = new NotificationInMemoryRepository();
        UUID userId = UUID.randomUUID();

        // when
        repository.deleteByUserIdAndEmitterId(userId, "emitter-1");

        // then
        assertThat(repository.findAllByUserId(userId)).isEmpty();
    }
}