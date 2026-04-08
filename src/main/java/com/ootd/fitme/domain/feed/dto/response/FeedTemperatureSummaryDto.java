package com.ootd.fitme.domain.feed.dto.response;

public record FeedTemperatureSummaryDto(
        Double current,
        Double comparedToDayBefore,
        Double min,
        Double max
) {
}
