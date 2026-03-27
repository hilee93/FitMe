package com.ootd.fitme.domain.feed.dto.response;

import java.util.UUID;

public record FeedAuthorSummaryDto(
        UUID userId,
        String name,
        String profileImageUrl
) {
}
