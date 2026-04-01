package com.ootd.fitme.domain.clothes.dto;

import com.ootd.fitme.domain.clothes.enums.ClothesType;

import java.util.List;
import java.util.UUID;

public record ClothesDto(
        UUID Id,
        UUID ownerId,
        String name,
        String imageUrl,
        ClothesType type,
        List<ClothesAttributeWithDefDto> attributes
) {
}
