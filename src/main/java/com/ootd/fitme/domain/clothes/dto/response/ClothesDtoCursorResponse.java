package com.ootd.fitme.domain.clothes.dto.response;

import com.ootd.fitme.domain.clothes.dto.ClothesDto;
import com.ootd.fitme.domain.clothes.enums.SortBy;
import com.ootd.fitme.domain.clothes.enums.SortDirection;

import java.util.List;

public record ClothesDtoCursorResponse(
        List<ClothesDto> data,
        String nextCursor,
        String nextIdAfter,
        boolean hasNext,
        long totalCount,
        SortBy sortBy,
        SortDirection sortDirection
) {
}
