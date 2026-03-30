package com.ootd.fitme.domain.comment.dto.response;

import com.ootd.fitme.domain.comment.enums.CommentSortCriteria;
import com.ootd.fitme.domain.comment.enums.SortDirection;
import com.ootd.fitme.domain.feed.dto.response.CursorResult;

import java.util.List;
import java.util.UUID;

public record CommentCursorResponseDto(
        List<CommentResponseDto> comments,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        long totalCount,
        CommentSortCriteria sortBy,
        SortDirection sortDirection
) {
    public static CommentCursorResponseDto from(CursorResult<CommentResponseDto> cursorResult) {
        List<CommentResponseDto> content = cursorResult.content();

        CommentResponseDto lastContent = content.isEmpty() ? null : content.get(content.size() - 1);
        String nextCursor = null;
        UUID nextIdAfter = null;

        if (cursorResult.hasNext() && lastContent != null) {
            nextCursor = lastContent.createdAt().toString();
            nextIdAfter = lastContent.id();
        }

        return new CommentCursorResponseDto(
                content,
                nextCursor,
                nextIdAfter,
                cursorResult.hasNext(),
                cursorResult.total(),
                CommentSortCriteria.CREATED_AT,
                SortDirection.DESCENDING
        );
    }
}
