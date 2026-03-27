package com.ootd.fitme.domain.attribute.exception;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;

import java.time.Instant;
import java.util.Map;

public class AttributeException extends FitmeException {
    public AttributeException(ErrorCode errorCode) {
        super(errorCode);
    }
    public AttributeException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
    public AttributeException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}