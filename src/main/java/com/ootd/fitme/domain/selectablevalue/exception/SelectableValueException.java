package com.ootd.fitme.domain.selectablevalue.exception;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;

import java.time.Instant;
import java.util.Map;

public class SelectableValueException extends FitmeException {
    public SelectableValueException(ErrorCode errorCode) {
        super(errorCode);
    }
    public SelectableValueException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
    public SelectableValueException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}