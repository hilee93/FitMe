package com.ootd.fitme.domain.notification.service;

import com.ootd.fitme.domain.notification.dto.response.NotificationDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SseMessage {

    private UUID eventId;
    private UUID receiverIds;
    private String eventName;
    private NotificationDto eventData;

    public static SseMessage create(UUID receiverIds, NotificationDto eventData) {
        return new SseMessage(
                UUID.randomUUID(),
                receiverIds,
                "notifications",
                eventData
        );
    }
    public Set<ResponseBodyEmitter.DataWithMediaType> toEvent() {
        return SseEmitter.event()
                .id(eventId.toString())
                .name(eventName)
                .data(eventData)
                .build();
    }

    public boolean isReceivable(UUID userId) {
        return this.receiverIds.equals(userId);
    }
}