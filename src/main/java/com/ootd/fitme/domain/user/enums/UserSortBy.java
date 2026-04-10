package com.ootd.fitme.domain.user.enums;

import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.exception.user.UserException;
import com.ootd.fitme.global.exception.ErrorCode;

import java.util.Arrays;

public enum UserSortBy {
    CREATED_AT("createdAt"),
    EMAIL("email");

    private final String value;

    UserSortBy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserSortBy from(String value) {
        if (value == null || value.isBlank()) {
            return CREATED_AT;
        }

        return Arrays.stream(values())
                .filter(v -> v.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new UserException(ErrorCode.INVALID_INPUT_VALUE));
    }

    public String extractCursor(UserDto dto) {
        return switch (this) {
            case CREATED_AT -> dto.createdAt().toString();
            case EMAIL -> dto.email();
        };
    }
}
