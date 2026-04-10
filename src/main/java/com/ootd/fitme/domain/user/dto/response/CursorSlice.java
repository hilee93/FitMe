package com.ootd.fitme.domain.user.dto.response;


import java.util.List;
import java.util.UUID;

public record CursorSlice<T>(
        List<T> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        long totalCount
) {
}
