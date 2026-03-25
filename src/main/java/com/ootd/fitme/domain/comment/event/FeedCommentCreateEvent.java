package com.ootd.fitme.domain.comment.event;

import java.time.Instant;
import java.util.UUID;

public record FeedCommentCreateEvent(
        UUID commentId,
        UUID feedId,
        UUID feedOwnerId,
        UUID commenterId,
        String commenterName,
        String comment,
        Instant createdAt
) {
}
