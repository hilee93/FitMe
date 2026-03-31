package com.ootd.fitme.domain.profile.exception;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;

import java.time.Instant;
import java.util.Map;

public class ProfileException extends FitmeException {
    public ProfileException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ProfileException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public ProfileException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
