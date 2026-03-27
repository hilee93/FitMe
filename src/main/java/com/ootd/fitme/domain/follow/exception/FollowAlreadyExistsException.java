package com.ootd.fitme.domain.follow.exception;

import com.ootd.fitme.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class FollowAlreadyExistsException extends FollowException {

    public FollowAlreadyExistsException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FollowAlreadyExistsException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public FollowAlreadyExistsException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
