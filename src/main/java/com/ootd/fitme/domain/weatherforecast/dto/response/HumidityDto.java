package com.ootd.fitme.domain.weatherforecast.dto.response;

public record HumidityDto(
        Double current,
        Double comparedToDayBefore
) {
}
