package com.ootd.fitme.domain.feed.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum SortCriteria {
    CREATED_AT("createdAt"),
    LIKE_COUNT("likeCount");

    private final String value;

    public static SortCriteria from(String value) {
        return Arrays.stream(values())
                .filter(sortCriteria -> sortCriteria.value.equals(value))
                .findFirst()
                .orElseThrow(); // TODO: INVALID_SORT_CRITERIA, sortValue 커스텀 예외추가
    }

}
