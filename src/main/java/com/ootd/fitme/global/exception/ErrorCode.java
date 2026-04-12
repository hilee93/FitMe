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
    USER_TEMP_PASSWORD_MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "임시 비밀번호 메일 발송에 실패했습니다.", "U-009"),

    // profile
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "프로필을 찾을 수 없습니다.", "P-001"),
    PROFILE_IMAGE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "프로필 이미지 저장에 실패했습니다.", "P-002"),
    PROFILE_IMAGE_TOO_LARGE(HttpStatus.BAD_REQUEST, "프로필 이미지는 5MB 이하여야 합니다.", "P-003"),
    PROFILE_IMAGE_CONTENT_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 형식입니다.", "P-004"),
    PROFILE_IMAGE_EXTENSION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 확장자입니다.", "P-005"),

    //notification
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND,"알림을 찾을 수 없습니다","NC-001"),
    NOTIFICATION_BAD_REQUEST(HttpStatus.BAD_REQUEST, "요청한 알림이 사용자와 일치하지 않습니다","NC-002"),

    // feed
    FEED_NOT_FOUND(HttpStatus.NOT_FOUND, "피드를 찾을 수 없습니다.", "F-001"),
    FEED_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 피드에 대한 권한이 없습니다", "F-002"),
    FEED_LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 피드에 좋아요를 눌렀습니다", "F-003"),
    FEED_LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 피드의 좋아요를 찾을 수 없습니다.", "F-004"),

    // comment
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다.", "C-001"),

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
    SELECTABLE_VALUE_INVALID(HttpStatus.BAD_REQUEST, "선택 옵션의 이름(타입) 데이터가 누락되거나 공백이어서 옵션을 생성할 수 없습니다.", "OPT_001"),

    // Clothes
    CLOTHES_OWNER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.", "C-001"),
    CLOTHES_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 옷을 찾을 수 없습니다.", "C-002"),
    OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 옵션 값입니다.", "C-004"),
    CLOTHES_NAME_INVALID(HttpStatus.BAD_REQUEST, "옷 이름은 필수입니다.", "C-005"),
    CLOTHES_TYPE_INVALID(HttpStatus.BAD_REQUEST, "옷 카테고리(타입)는 필수입니다.", "C-006"),
    CLOTHES_USER_INVALID(HttpStatus.BAD_REQUEST, "옷의 소유자(User)는 필수입니다.", "C-007"),

    // follow
    FOLLOW_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 팔로우한 사용자입니다.", "FL-001"),
    FOLLOW_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 팔로우입니다.", "FL-002"),
    FOLLOW_SELF_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자신을 팔로우 할 수 없습니다.", "FL-003"),

    // direct message
    DM_SENDER_MISMATCH(HttpStatus.FORBIDDEN, "메시지 발신자 정보가 일치하지 않습니다.", "DM-001"),

    // recommendation
    RECOMMENDATION_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저 정보를 찾을 수 없습니다.", "R-001"),
    RECOMMENDATION_WEATHER_NOT_FOUND(HttpStatus.NOT_FOUND, "날씨 정보를 찾을 수 없습니다.", "R-002"),
    RECOMMENDATION_PROFILE_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "유저의 프로필 데이터를 찾을 수 없습니다.", "R-003"),

    // storage
    INVALID_STORAGE_KEY(HttpStatus.INTERNAL_SERVER_ERROR, "스토리지 연결 키가 유효하지 않거나 설정되지 않았습니다.", "S-001"),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.", "S-002"),
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다.", "S-003"),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제 실패했습니다.", "S-004"),

    // scraper
    SCRAP_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,"해당 링크에 접근할 수 없거나 보안에 막혔습니다.","SCRAP-001"),
    UNSUPPORTED_DOMAIN(HttpStatus.INTERNAL_SERVER_ERROR,"해당 쇼핑몰은 현재 지원하지 않는 도메인입니다.","SCRAP-002"),

    //ai
    ERROR_OCCURRED_DURING_ANALYSIS(HttpStatus.INTERNAL_SERVER_ERROR, "AI 데이터 분석 중 오류가 발생했습니다.", "AI-001"),

    //ai
    ERROR_OCCURRED_DURING_ANALYSIS(HttpStatus.INTERNAL_SERVER_ERROR, "AI 데이터 분석 중 오류가 발생했습니다.", "AI-001"),

    //mediafile
    MEDIA_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 미디어 파일입니다.", "MEDIA-001"),
    MEDIA_FILE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 파일에 대한 접근 권한이 없습니다.", "MEDIA-002"),
    INVALID_FILE_REQUEST(HttpStatus.BAD_REQUEST, "파일이 비어있거나 잘못된 요청입니다.", "MEDIA-003"),
    UNSUPPORTED_FILE_FORMAT(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 이미지 파일 형식입니다.", "MEDIA-004");


    private final String message;
    private final HttpStatus status;
    private final String code;

    ErrorCode(HttpStatus status, String message, String code) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

}
