package com.ootd.fitme.domain.recommendation.exception;

import com.ootd.fitme.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class RecommendationProfileDataNotFoundException extends RecommendationException {
    public RecommendationProfileDataNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
    public RecommendationProfileDataNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
    public RecommendationProfileDataNotFoundException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
