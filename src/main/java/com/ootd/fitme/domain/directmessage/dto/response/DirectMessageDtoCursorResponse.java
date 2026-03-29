package com.ootd.fitme.domain.directmessage.dto.response;

import com.ootd.fitme.domain.directmessage.enums.SortBy;
import com.ootd.fitme.domain.directmessage.enums.SortDirection;

import java.util.List;
import java.util.UUID;

public record DirectMessageDtoCursorResponse(
        List<DirectMessageDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        long totalCount,
        SortBy sortBy,
        SortDirection sortDirection
) {}
