package com.ootd.fitme.domain.attribute.exception;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;

public class AttributeException extends FitmeException {
    public AttributeException(ErrorCode errorCode) {
        super(errorCode);
    }
}