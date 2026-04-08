package com.ootd.fitme.domain.comment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum CommentSortCriteria {
    CREATED_AT("createdAt");

    private final String value;

    public static CommentSortCriteria from(String value) {
        return Arrays.stream(values())
                .filter(commentSortCriteria -> commentSortCriteria.value.equals(value))
                .findFirst()
                .orElseThrow(); // TODO: INVALID_SORT_CRITERIA, sortValue 커스텀 예외추가
    }

}
