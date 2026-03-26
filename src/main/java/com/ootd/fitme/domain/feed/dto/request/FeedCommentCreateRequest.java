package com.ootd.fitme.domain.feed.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FeedCommentCreateRequest(
        @NotNull UUID feedId,
        @NotNull UUID authorId,
        @NotBlank String content
) {
}
