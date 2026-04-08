package com.ootd.fitme.domain.recommendation.dto.response;

import java.util.List;
import java.util.UUID;

public record RecommendationClothAttributeSummaryDto(
        UUID definitionId,
        String definitionName,
        List<String> selectableValues,
        String value
) {

}

