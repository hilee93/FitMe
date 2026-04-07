package com.ootd.fitme.domain.clothes.dto;

import com.ootd.fitme.domain.clothes.enums.ClothesType;
import java.util.List;

public record ExtractedClothesInfo(
        String name,
        ClothesType type,
        String imageUrl,
        List<ClothesAttributeDto> attributes
) {
}