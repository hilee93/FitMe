package com.ootd.fitme.domain.feedlike.event;

import java.time.Instant;
import java.util.UUID;

public record FeedLikedCreateEvent(
        UUID feedLikeId,
        UUID feedId,
        UUID likedId,
        UUID likerId,
        String likerName,
        Instant createdAt
) {
}
