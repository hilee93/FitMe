package com.ootd.fitme.infrastructure.ai.exception;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;

import java.time.Instant;
import java.util.Map;

public class AiException extends FitmeException {
    public AiException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }

    public AiException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public AiException(ErrorCode errorCode) {
        super(errorCode);
    }
}
