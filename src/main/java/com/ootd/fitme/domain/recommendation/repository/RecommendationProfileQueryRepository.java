package com.ootd.fitme.domain.recommendation.repository;

import com.ootd.fitme.domain.recommendation.dto.response.RecommendationProfileSummaryDto;

import java.util.Optional;
import java.util.UUID;

public interface RecommendationProfileQueryRepository {
    Optional<RecommendationProfileSummaryDto> findProfileByUserId(UUID userId);
}
