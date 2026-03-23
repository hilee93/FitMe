package com.ootd.fitme.domain.feed.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FeedResponseDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        FeedAuthorSummaryDto author,
        FeedWeatherSummaryDto weather,
        List<FeedClothesSummaryDto> ootds,
        String content,
        int likeCount,
        int commentCount,
        boolean likedByMe
) {
}
