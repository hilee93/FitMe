package com.ootd.fitme.infrastructure.scraper.exception;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;

import java.time.Instant;
import java.util.Map;

public class ScraperException extends FitmeException {
    public ScraperException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ScraperException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public ScraperException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
