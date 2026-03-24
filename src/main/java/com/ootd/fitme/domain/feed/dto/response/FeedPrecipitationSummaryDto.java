package com.ootd.fitme.domain.feed.dto.response;

import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;

public record FeedPrecipitationSummaryDto(
        PrecipitationType type,
        Double amount,
        Double probability
) {
}
