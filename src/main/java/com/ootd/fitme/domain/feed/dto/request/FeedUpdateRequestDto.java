package com.ootd.fitme.domain.feed.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeedUpdateRequestDto(
      @NotBlank
      @Size(max = 300)
      String content
) {
}
