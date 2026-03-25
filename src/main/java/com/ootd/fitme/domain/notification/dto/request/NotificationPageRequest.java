package com.ootd.fitme.domain.notification.dto.request;

import java.util.UUID;

public record NotificationPageRequest(
        UUID userId,
        String cursor,
        String idAfter,
        int limit
) {
}
