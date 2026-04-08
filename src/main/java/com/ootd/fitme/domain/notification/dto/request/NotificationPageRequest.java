package com.ootd.fitme.domain.notification.dto.request;

import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record NotificationPageRequest(
        UUID userId,
        String cursor,
        String idAfter,
        int limit
) {
    public static NotificationPageRequest from(
            CustomUserPrincipal principal,
            NotificationPageQueryRequest query
    ) {
        int limit = query.limit() == null ? 20 : query.limit();

        return new NotificationPageRequest(
                principal.getUserId(),
                query.cursor(),
                query.idAfter(),
                limit
        );
    }
}
