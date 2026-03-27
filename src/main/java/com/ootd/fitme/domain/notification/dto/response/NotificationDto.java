package com.ootd.fitme.domain.notification.dto.response;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        Instant createdAt,
        UUID userId,
        String title,
        String content,
        String level

) {
}
