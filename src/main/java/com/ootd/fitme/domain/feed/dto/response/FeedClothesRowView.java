package com.ootd.fitme.domain.feed.dto.response;

import com.ootd.fitme.domain.clothes.enums.ClothesType;

import java.util.UUID;

public interface FeedClothesRowView {
    UUID clothesId();
    UUID attributeDefinitionId();
    String attributeDefinitionName();
    String attributeValue();

    String clothesName();
    String imageUrl();
    ClothesType clothesType();
}
