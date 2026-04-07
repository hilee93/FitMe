package com.ootd.fitme.domain.recommendation.dto.response;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record RecommendationDto(
        @NotNull UUID weatherId,
        @NotNull UUID userId,
        List<RecommendationClothesSummaryDto> clothes
) {
}
