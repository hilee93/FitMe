package com.ootd.fitme.domain.recommendation.dto.response;

import com.ootd.fitme.domain.clothes.enums.ClothesType;

import java.util.UUID;

public record RecommendationClothesSummaryDto(
        UUID id,
        String name,
        ClothesType type,
        String imageUrl
) {
}
