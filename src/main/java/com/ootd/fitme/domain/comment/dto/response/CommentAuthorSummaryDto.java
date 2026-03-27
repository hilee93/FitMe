package com.ootd.fitme.domain.comment.dto.response;

import java.util.UUID;

public record CommentAuthorSummaryDto(
        UUID userId,
        String name,
        String profileImageUrl
) {
}
