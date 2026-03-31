package com.ootd.fitme.domain.comment.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CommentSearchCondition(
        String cursor,
        UUID idAfter,
        @NotNull Integer limit,
        @NotNull UUID feedId
) {

}

