package com.ootd.fitme.domain.follow.exception;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;

import java.time.Instant;
import java.util.Map;

public class FollowException extends FitmeException {

    public FollowException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FollowException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public FollowException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
