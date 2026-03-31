package com.ootd.fitme.domain.clothes.dto.request;

import com.ootd.fitme.domain.clothes.dto.ClothesAttributeDto;
import com.ootd.fitme.domain.clothes.enums.ClothesType;

import java.util.List;

public record ClothesUpdateRequest(
        String name,
        ClothesType type,
        List<ClothesAttributeDto> attributes
) {
}
