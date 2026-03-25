package com.ootd.fitme.domain.follow.dto.response;

import com.ootd.fitme.domain.follow.enums.SortDirection;

import java.util.List;
import java.util.UUID;

public record FollowListResponse (
        List<FollowDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        long totalCount,
        String sortBy,
        SortDirection sortDirection
) {}
