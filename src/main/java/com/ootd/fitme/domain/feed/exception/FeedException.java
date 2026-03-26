package com.ootd.fitme.domain.feed.exception;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;

import java.time.Instant;
import java.util.Map;

public class FeedException extends FitmeException {

    public FeedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FeedException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public FeedException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }

}
