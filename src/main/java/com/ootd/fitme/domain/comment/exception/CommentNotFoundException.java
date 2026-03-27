package com.ootd.fitme.domain.comment.exception;

import com.ootd.fitme.global.exception.ErrorCode;

import java.time.Instant;
import java.util.Map;

public class CommentNotFoundException extends CommentException {
    public CommentNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CommentNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public CommentNotFoundException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
        super(timestamp, errorCode, details);
    }
}
