package com.ootd.fitme.domain.notification.dto.response;

import com.ootd.fitme.domain.notification.entity.Notification;
import com.ootd.fitme.domain.notification.mapper.NotificationMapper;
import org.springframework.data.domain.Slice;

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

    public static NotificationPageResponse from(Slice<Notification> slice, long totalElements) {
        List<Notification> notifications = slice.getContent();

        List<NotificationDto> content = notifications.stream()
                .map(NotificationMapper::toDto)
                .toList();

        String nextCursor = null;
        String nextIdAfter = null;

        if (slice.hasNext() && !notifications.isEmpty()) {
            Notification last = notifications.get(notifications.size() - 1);
            nextCursor = last.getCreatedAt().toString();
            nextIdAfter = last.getId().toString();
        }

        return new NotificationPageResponse(
                content,
                nextCursor,
                nextIdAfter,
                slice.hasNext(),
                totalElements,
                "createdAt",
                "DESCENDING"
        );
    }
}
