package com.ootd.fitme.domain.recommendation.exception;

import com.ootd.fitme.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class RecommendationWeatherNotFoundException extends RecommendationException {
    public RecommendationWeatherNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
    public RecommendationWeatherNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
    public RecommendationWeatherNotFoundException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
