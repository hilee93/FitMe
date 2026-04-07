package com.ootd.fitme.domain.recommendation.service;

import com.ootd.fitme.domain.recommendation.dto.response.RecommendationDto;

import java.util.UUID;

public interface RecommendationService {
    RecommendationDto recommendation (UUID userId, UUID weatherId);
}
