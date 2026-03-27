package com.ootd.fitme.domain.feed.exception;

import com.ootd.fitme.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class FeedNotFoundException extends FeedException {
    public FeedNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FeedNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public FeedNotFoundException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
