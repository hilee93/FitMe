package com.ootd.fitme.domain.feed.dto.response;

import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;

import java.util.UUID;

public record FeedWeatherSummaryDto(
        UUID weatherId,
        SkyStatus skyStatus,
        FeedPrecipitationSummaryDto precipitation,
        FeedTemperatureSummaryDto temperature
) {
}
