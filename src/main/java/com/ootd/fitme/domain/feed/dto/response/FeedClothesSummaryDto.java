package com.ootd.fitme.domain.feed.dto.response;

import com.ootd.fitme.domain.clothes.enums.ClothesType;

import java.util.List;
import java.util.UUID;

public record FeedClothesSummaryDto(
        UUID clothesId,
        String name,
        String imageUrl,
        ClothesType type,
        List<FeedAttributeSummaryDto> attributes
) {
}
