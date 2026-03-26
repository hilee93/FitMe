package com.ootd.fitme.domain.feed.dto.response;

import com.ootd.fitme.domain.clothes.enums.ClothesType;

import java.util.UUID;

public record FeedClothesFlatRow(
        UUID clothesId,
        String clothesName,
        String imageUrl,
        ClothesType clothesType,
        UUID attributeDefinitionId,
        String attributeDefinitionName,
        String attributeValue
) {
}
