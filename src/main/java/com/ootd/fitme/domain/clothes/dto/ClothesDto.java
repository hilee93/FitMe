package com.ootd.fitme.domain.clothes.dto;

import com.ootd.fitme.domain.clothes.enums.ClothesType;

public record ClothesDto(
        String Id,
        String ownerId,
        String name,
        String imageUrl,
        ClothesType type

) {
}
