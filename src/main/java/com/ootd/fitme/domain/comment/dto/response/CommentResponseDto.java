package com.ootd.fitme.domain.comment.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CommentResponseDto(
        UUID id,
        Instant createdAt,
        UUID feedId,
        CommentAuthorSummaryDto author,
        String Content
) {
}
