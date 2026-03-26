package com.ootd.fitme.domain.notification.exception;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;

import java.time.Instant;
import java.util.Map;

public class NotificationException extends FitmeException {
    public NotificationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotificationException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public NotificationException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
