package com.ootd.fitme.domain.feed.exception;

import com.ootd.fitme.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class FeedLikeAlreadyExistsException extends FeedException {
    public FeedLikeAlreadyExistsException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FeedLikeAlreadyExistsException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public FeedLikeAlreadyExistsException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
