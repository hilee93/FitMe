package com.ootd.fitme.domain.weatherforecast.dto.response;

public record TemperatureDto(
        Double current,
        Double comparedToDayBefore,
        Double min,
        Double max
) {
}
