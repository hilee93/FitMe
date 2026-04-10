package com.ootd.fitme.domain.notification.event;

import com.ootd.fitme.domain.notification.entity.Notification;
import com.ootd.fitme.domain.notification.enums.NotificationLevel;
import com.ootd.fitme.domain.notification.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationCreatedEvent(
        UUID notificationId,
        UUID receiverId,
        Instant createdAt,
        String title,
        String content,
        NotificationLevel level,
        NotificationType type
) {
    public static NotificationCreatedEvent from(Notification notification) {
        return new NotificationCreatedEvent(
                notification.getId(),
                notification.getUser().getId(),
                notification.getCreatedAt(),
                notification.getTitle(),
                notification.getContent(),
                notification.getLevel(),
                notification.getType()
        );
    }
}
