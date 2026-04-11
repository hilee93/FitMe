package com.ootd.fitme.domain.notification.enums;

import lombok.Getter;

@Getter
public enum AttributeAction {
    ADDED("속성이 추가되었습니다."),
    UPDATED("속성이 수정되었습니다."),
    REMOVED("속성이 삭제되었습니다.");

    private final String message;

    AttributeAction(String message) {
        this.message = message;
    }
}
