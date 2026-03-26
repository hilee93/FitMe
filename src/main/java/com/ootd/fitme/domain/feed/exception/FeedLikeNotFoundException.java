package com.ootd.fitme.domain.feed.exception;

import com.ootd.fitme.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class FeedLikeNotFoundException extends FeedException {
    public FeedLikeNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FeedLikeNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public FeedLikeNotFoundException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
