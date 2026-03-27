package com.ootd.fitme.domain.notification.dto.response;

import java.util.List;

public record NotificationPageResponse(
        List<NotificationDto> content,
        String nextCursor,
        String nextIdAfter,
        boolean hasNext,
        long totalElements,
        String sortBy,
        String sortDirection
) {
}
