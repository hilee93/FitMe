package com.ootd.fitme.domain.follow.exception;

import com.ootd.fitme.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class FollowSelfNotAllowedException extends FollowException {

    public FollowSelfNotAllowedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FollowSelfNotAllowedException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public FollowSelfNotAllowedException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
