package com.ootd.fitme.domain.comment.event;

import java.time.Instant;
import java.util.UUID;

public record FeedCommentCreateEvent(
        UUID commenterId,
        UUID userId,
        String commenterName,
        String comment,
        Instant createdAt
) {
}
