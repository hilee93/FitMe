package com.ootd.fitme.domain.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
@Repository
public class SseMessageRepository {

    private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
    private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

    public SseMessage save(SseMessage message) {
        makeAvailableCapacity();

        UUID eventId = message.getEventId();
        eventIdQueue.addLast(eventId);
        messages.put(eventId, message);
        log.debug("sse 메시지저장{}", eventId);
        log.debug("sse 재연결 맵크기 = {}", messages.size());
        return message;
    }

    public List<SseMessage> findAllByEventIdAfterAndReceiverId(UUID eventId, UUID receiverId) {
        return eventIdQueue.stream()
                .dropWhile(data -> !data.equals(eventId))
                .skip(1)
                .map(messages::get)
                .filter(msg -> msg.isReceivable(receiverId))
                .toList();
    }

    private void makeAvailableCapacity() {
        int eventQueueCapacity = 1000;
        int availableCapacity = eventQueueCapacity - eventIdQueue.size();
        while (availableCapacity < 1) {
            UUID removedEventId = eventIdQueue.removeFirst();
            messages.remove(removedEventId);
            availableCapacity++;
        }
    }
}
