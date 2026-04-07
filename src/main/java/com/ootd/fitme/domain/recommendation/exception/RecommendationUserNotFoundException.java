package com.ootd.fitme.domain.recommendation.exception;

import com.ootd.fitme.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class RecommendationUserNotFoundException extends RecommendationException {
    public RecommendationUserNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public RecommendationUserNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public RecommendationUserNotFoundException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
