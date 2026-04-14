package com.ootd.fitme.domain.clothes.dto;

import com.ootd.fitme.domain.clothes.enums.ClothesType;

import java.util.List;

public record AiClothesResult(
        String name,
        ClothesType type,
        List<AiAttribute> attributes
) {
    public record AiAttribute(
            String definitionName, // 예: "색상"
            String value           // 예: "파랑"
    ) {}
}