package com.ootd.fitme.domain.mediafile.exception;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;

import java.time.Instant;
import java.util.Map;

public class MediaFileException extends FitmeException {
    public MediaFileException(ErrorCode errorCode) {
        super(errorCode);
    }

    public MediaFileException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public MediaFileException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
