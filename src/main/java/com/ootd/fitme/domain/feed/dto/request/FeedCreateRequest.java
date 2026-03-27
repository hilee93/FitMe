package com.ootd.fitme.domain.feed.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record FeedCreateRequest(
        @NotNull UUID authorId,
        @NotNull UUID weatherId,
        @NotEmpty List<@NotNull UUID> clothesIds,
        @NotBlank @Size(max = 300) String content
) {
}
