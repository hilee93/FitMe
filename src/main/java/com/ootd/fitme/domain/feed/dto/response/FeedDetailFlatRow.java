package com.ootd.fitme.domain.feed.dto.response;

import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;

import java.time.Instant;
import java.util.UUID;

public record FeedDetailFlatRow(
        UUID feedId,
        Instant createdAt,
        Instant updatedAt,
        String content,
        int likeCount,
        int commentCount,
        UUID authorId,
        UUID weatherId,
        SkyStatus skyStatus,
        PrecipitationType precipitationType,
        Double precipitationAmount,
        Double precipitationProbability,
        Double currentTemperature,
        Double comparedToDayBefore,
        Double temperatureMin,
        Double temperatureMax
) {
}
