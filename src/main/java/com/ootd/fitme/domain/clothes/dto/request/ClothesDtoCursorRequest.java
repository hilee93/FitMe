package com.ootd.fitme.domain.clothes.dto.request;

import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.clothes.enums.SortBy;
import com.ootd.fitme.domain.clothes.enums.SortDirection;

public record ClothesDtoCursorRequest(
        String cursor,
        String idAfter,
        int limit,
        ClothesType typeEqual,
        String ownerId,
        SortBy sortBy,
        SortDirection sortDirection

) {
}
