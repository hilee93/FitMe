package com.ootd.fitme.domain.feed.dto.response.elasticsearch;

import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;

import java.time.Instant;
import java.util.UUID;

public record FeedSearchHitRow(
        UUID feedId,
        UUID authorId,
        UUID weatherId,
        String content,
        int likeCount,
        int commentCount,
        SkyStatus skyStatus,
        PrecipitationType precipitationType,
        Instant createdAt,
        Instant updatedAt
) {
}
