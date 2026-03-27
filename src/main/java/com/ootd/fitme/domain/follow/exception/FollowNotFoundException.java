package com.ootd.fitme.domain.follow.exception;

import com.ootd.fitme.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class FollowNotFoundException extends FollowException {

    public FollowNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FollowNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public FollowNotFoundException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
