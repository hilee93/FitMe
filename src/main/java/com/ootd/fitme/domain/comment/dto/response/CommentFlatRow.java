package com.ootd.fitme.domain.comment.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CommentFlatRow(
        UUID id,
        Instant createdAt,
        UUID feedId,
        UUID userId,
        String name,
        String profileImageUrl,
        String content
) {

}
