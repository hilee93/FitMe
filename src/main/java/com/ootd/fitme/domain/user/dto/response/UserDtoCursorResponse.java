package com.ootd.fitme.domain.user.dto.response;

import com.ootd.fitme.domain.user.enums.SortDirection;

import java.util.List;
import java.util.UUID;

public record UserDtoCursorResponse(
        List<UserDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        long totalCount,
        String sortBy,
        SortDirection sortDirection
) {
}
