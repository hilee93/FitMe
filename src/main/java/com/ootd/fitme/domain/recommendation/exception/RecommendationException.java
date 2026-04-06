package com.ootd.fitme.domain.recommendation.exception;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;

import java.time.Instant;
import java.util.Map;

public class RecommendationException extends FitmeException {
    public RecommendationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public RecommendationException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public RecommendationException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
