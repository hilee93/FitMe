package com.ootd.fitme.domain.feed.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FeedCommentSearchCondition(
        @NotNull  UUID feedId,
        String cursor,
        UUID idAfter,
        Integer limit
) {
    public FeedCommentSearchCondition {
        if (limit == null || limit <= 0) limit = 20;
    }
}
