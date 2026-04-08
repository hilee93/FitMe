package com.ootd.fitme.domain.comment.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CommentResponseDto(
        UUID id,
        Instant createdAt,
        UUID feedId,
        CommentAuthorSummaryDto author,
        String content
) {
    
    public static CommentResponseDto from(CommentFlatRow row) {
        return new CommentResponseDto(
                row.id(),
                row.createdAt(),
                row.feedId(),
                new CommentAuthorSummaryDto(
                        row.userId(),
                        row.name(),
                        row.profileImageUrl()
                ),
                row.content()
        );
    }
}
