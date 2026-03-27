package com.ootd.fitme.domain.feed.exception;

import com.ootd.fitme.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class FeedAccessDeniedException extends FeedException {
    public FeedAccessDeniedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FeedAccessDeniedException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public FeedAccessDeniedException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
