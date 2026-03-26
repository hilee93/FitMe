package com.ootd.fitme.domain.notification.dto.request;

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
}
