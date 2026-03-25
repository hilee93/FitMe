package com.ootd.fitme.domain.notification.dto.request;

import com.ootd.fitme.domain.clothes.enums.SortBy;
import com.ootd.fitme.domain.notification.dto.response.NotificationDto;

import java.time.Instant;
import java.time.LocalDateTime;
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
