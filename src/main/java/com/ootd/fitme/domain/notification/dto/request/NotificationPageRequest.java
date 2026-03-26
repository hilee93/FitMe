package com.ootd.fitme.domain.notification.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record NotificationPageRequest(

        @NotNull(message = "userId는 필수입니다")
        UUID userId,

        @Pattern(
                regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?Z$",
                message = "cursor는 ISO-8601 UTC 형식이어야 합니다"
        )
        String cursor,

        @Pattern(
                regexp = "^[0-9a-fA-F-]{36}$",
                message = "idAfter는 UUID 형식이어야 합니다"
        )
        String idAfter,

        @Min(value = 1, message = "limit는 1 이상이어야 합니다")
        @Max(value = 100, message = "limit는 100 이하로 설정하세요")
        int limit
) {
}
