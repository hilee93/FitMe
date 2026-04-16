package com.ootd.fitme.domain.feed.dto.response;

import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;

import java.util.UUID;

public record FeedWeatherFlatRow(
        UUID weatherId,
        SkyStatus skyStatus,
        PrecipitationType precipitationType,
        Double precipitationAmount,
        Double precipitationProbability,
        Double temperatureCurrent,
        Double temperatureComparedToDayBefore,
        Double temperatureMin,
        Double temperatureMax
) {
}
