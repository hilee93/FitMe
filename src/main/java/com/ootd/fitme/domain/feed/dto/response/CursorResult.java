package com.ootd.fitme.domain.feed.dto.response;

import java.util.List;

public record CursorResult<T>(
        List<T> content,
        boolean hasNext,
        long total
) {
}
