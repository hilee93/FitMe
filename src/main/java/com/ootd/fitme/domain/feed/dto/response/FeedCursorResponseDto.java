package com.ootd.fitme.domain.feed.dto.response;

import com.ootd.fitme.domain.feed.enums.FeedSortCriteria;
import com.ootd.fitme.domain.feed.enums.SortDirection;

import java.util.List;
import java.util.UUID;

public record FeedCursorResponseDto(
        List<FeedResponseDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        int totalCount,
        FeedSortCriteria sortBy,
        SortDirection sortDirection
) {
}
