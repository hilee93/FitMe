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
        String sortBy,
        SortDirection sortDirection
) {


    public static FeedCursorResponseDto from(
            CursorResult<FeedBaseFlatRow> cursorResult,
            List<FeedResponseDto> feedResponseDtoList,
            FeedSortCriteria sortBy,
            SortDirection sortDirection
    ) {

        List<FeedBaseFlatRow> data = cursorResult.content();

        FeedBaseFlatRow lastData = data.isEmpty() ? null : data.get(data.size() - 1);

        String nextCursor = null;
        UUID nextIdAfter = null;


        if (cursorResult.hasNext() && lastData != null) {
            nextCursor = extractCursor(sortBy, lastData);
            nextIdAfter = lastData.feedId();
        }

        return new FeedCursorResponseDto(
                feedResponseDtoList,
                nextCursor,
                nextIdAfter,
                cursorResult.hasNext(),
                (int) cursorResult.total(),
                sortBy.getValue(),
                sortDirection
                );
    }

    private static String extractCursor(FeedSortCriteria sortBy, FeedBaseFlatRow last) {
        return switch (sortBy) {
            case CREATED_AT -> last.createdAt().toString();
            case LIKE_COUNT -> String.valueOf(last.likeCount());
        };
    }
}
