package com.ootd.fitme.domain.feed.event;

import java.time.Instant;
import java.util.UUID;

public record FeedCreateEvent(
        UUID feedId,
        UUID userId,
        String content,
        Instant createdAt
) {
}
