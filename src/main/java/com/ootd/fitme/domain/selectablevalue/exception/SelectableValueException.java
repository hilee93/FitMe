package com.ootd.fitme.domain.selectablevalue.exception;

import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;

public class SelectableValueException extends FitmeException {
    public SelectableValueException(ErrorCode errorCode) {
        super(errorCode);
    }
}