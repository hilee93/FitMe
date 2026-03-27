package com.ootd.fitme.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // user & auth
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 올바르지 않습니다.", "U-001"),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", "U-002"),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "신뢰할 수 없는 요청입니다.", "U-003"),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다.", "U-004"),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "토큰을 찾을 수 없습니다.", "U-005"),
    USER_ACCOUNT_LOCKED(HttpStatus.LOCKED, "유저 계정이 잠겨있습니다.", "U-006"),
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "유저가 이미 존재합니다.", "U-007"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다.", "U-008"),

    // feed
    FEED_NOT_FOUND(HttpStatus.NOT_FOUND, "피드를 찾을 수 없습니다.", "F-001"),
    FEED_ACCESS_DENIED(HttpStatus.FORBIDDEN, "요청에 대한 권한이 없습니다" , "F-002"),

    // common
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청이 올바르지 않습니다.", "CM-001"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", "CM-002"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다.", "CM-003"),
    INVALID_STATE(HttpStatus.CONFLICT, "요청을 처리할 수 없는 상태입니다.", "CM-004"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "요청값이 올바르지 않습니다.", "CM-005"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 미디어 타입입니다.", "CM-006"),

    //attribute
    ATTRIBUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 ID의 의상 속성 데이터가 존재하지 않아 조회 및 수정/삭제를 수행할 수 없습니다.", "ATTR_001"),
    ATTRIBUTE_NAME_INVALID(HttpStatus.BAD_REQUEST, "의상 속성의 이름 데이터가 누락되거나 공백이어서 속성을 생성/수정할 수 없습니다.", "ATTR_002"),
    ATTRIBUTE_NAME_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 속성 이름입니다.", "ATTR_003"),

    //selectable value
    SELECTABLE_VALUE_INVALID(HttpStatus.BAD_REQUEST, "선택 옵션의 이름(타입) 데이터가 누락되거나 공백이어서 옵션을 생성할 수 없습니다.", "OPT_001");

    private final String message;
    private final HttpStatus status;
    private final String code;

    ErrorCode(HttpStatus status, String message, String code) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

}
