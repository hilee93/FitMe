package com.ootd.fitme.domain.recommendation.repository;

import com.ootd.fitme.domain.recommendation.dto.response.RecommendationClothAttributeSummaryDto;
import com.ootd.fitme.domain.recommendation.dto.response.RecommendationClothesSummaryDto;

import java.util.List;
import java.util.UUID;

public interface RecommendationClothesQueryRepository {
    List<RecommendationClothesSummaryDto> findClothesByUserId(UUID userId);
    List<RecommendationClothAttributeSummaryDto> getAttributesForClothes(UUID clothesId);
}
