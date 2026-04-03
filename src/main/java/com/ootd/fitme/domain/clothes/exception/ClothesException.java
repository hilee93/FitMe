package com.ootd.fitme.domain.clothes.exception;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;

import java.time.Instant;
import java.util.Map;

public class ClothesException extends FitmeException {

    public ClothesException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ClothesException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public ClothesException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
