package com.ootd.fitme.domain.clothes.dto.request;

import com.ootd.fitme.domain.clothes.enums.ClothesType;

public record ClothesDtoCursorRequest(
        String cursor,
        String idAfter,
        int limit,
        ClothesType typeEqual,
        String ownerId

) {
}
