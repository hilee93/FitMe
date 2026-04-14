package com.ootd.fitme.domain.feed.event;

import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;

import java.time.Instant;
import java.util.UUID;

public record FeedUpdateEvent(
    UUID feedId,
    UUID userId,
    String content,
    Instant createdAt,
    Instant updatedAt,
    int likeCount,
    int commentCount,
    UUID weatherForecastId,
    SkyStatus skyStatus,
    PrecipitationType precipitationType
) {
}
