package com.ootd.fitme.domain.comment.event;

import java.time.Instant;
import java.util.UUID;

public record FeedCommentCreateEvent(
        UUID commentId,
        UUID feedId,
        UUID feedOwnerId,
        String content,
        UUID commenterId,
        String comment,
        Instant createdAt
) {
}
