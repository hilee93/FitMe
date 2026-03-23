package com.ootd.fitme.domain.user.exception.user;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;

import java.time.Instant;
import java.util.Map;

public class UserException extends FitmeException {
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public UserException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
