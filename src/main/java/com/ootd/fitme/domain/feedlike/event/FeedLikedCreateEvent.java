package com.ootd.fitme.domain.feedlike.event;

import java.time.Instant;
import java.util.UUID;

public record FeedLikedCreateEvent(
        UUID feedId,
        UUID likeId,
        UUID likedId,
        UUID likerId,
        String likerName,
        Instant createdAt
) {
}
