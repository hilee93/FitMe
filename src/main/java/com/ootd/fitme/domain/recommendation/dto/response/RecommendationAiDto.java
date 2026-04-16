package com.ootd.fitme.domain.recommendation.dto.response;

import java.util.List;
import java.util.UUID;

public record RecommendationAiDto(
        List<UUID> selectedClothesIds,
        String reason
) {
}
