package com.ootd.fitme.domain.clothes.dto.response;

import com.ootd.fitme.domain.clothes.dto.ClothesDto;

import java.util.List;

public record ClothesDtoCursorResponse(
        List<ClothesDto> contents,
        String nextCursor,
        boolean hasNext
) {
}
