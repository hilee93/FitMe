package com.ootd.fitme.domain.directmessage.exception;

import com.ootd.fitme.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class DirectMessageSenderMisMatchException extends DirectMessageException {

    public DirectMessageSenderMisMatchException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DirectMessageSenderMisMatchException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public DirectMessageSenderMisMatchException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
