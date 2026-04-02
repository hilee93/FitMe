package com.ootd.fitme.domain.directmessage.exception;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;

import java.time.Instant;
import java.util.Map;

public class DirectMessageException extends FitmeException {
  public DirectMessageException(ErrorCode errorCode) {
    super(errorCode);
  }

  public DirectMessageException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public DirectMessageException(Instant timestamp, ErrorCode errorCode, Map<String, Object> details) {
    super(timestamp, errorCode, details);
  }
}
