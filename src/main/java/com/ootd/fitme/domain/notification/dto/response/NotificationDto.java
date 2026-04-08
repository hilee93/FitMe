package com.ootd.fitme.domain.notification.dto.response;

import com.ootd.fitme.domain.notification.enums.NotificationLevel;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        Instant createdAt,
        UUID receiverId,
        String title,
        String content,
        NotificationLevel level

) {
}
