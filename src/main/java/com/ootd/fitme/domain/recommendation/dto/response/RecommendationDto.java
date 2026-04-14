package com.ootd.fitme.domain.recommendation.dto.response;

import java.util.List;
import java.util.UUID;

public record RecommendationDto(
        UUID weatherId,
        UUID userId,
        List<RecommendationClothesSummaryDto> clothes,
        String recommendationReason
) {
}
