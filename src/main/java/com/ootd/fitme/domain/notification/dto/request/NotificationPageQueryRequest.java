package com.ootd.fitme.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


public record NotificationPageQueryRequest(
        @Pattern(
                regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?Z$",
                message = "cursor는 ISO-8601 UTC 형식이어야 합니다"
        )
        @Schema(description = "커서(createdAt)", example = "2026-03-27T09:00:00Z")
        String cursor,

        @Pattern(
                regexp = "^[0-9a-fA-F-]{36}$",
                message = "idAfter는 UUID 형식이어야 합니다"
        )
        @Schema(description = "동일 createdAt에서 다음 조회 기준 ID", example = "11111111-1111-1111-1111-111111111111",format = "uuid")
        String idAfter,

        @Min(value = 1, message = "limit는 1 이상이어야 합니다")
        @Max(value = 100, message = "limit는 100 이하로 설정하세요")
        @Schema(description = "조회 개수", example = "20", defaultValue = "20")
        Integer limit
) {

}
