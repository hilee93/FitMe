package com.ootd.fitme.domain.weatherforecast.dto.response;

import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;

import java.time.Instant;
import java.util.UUID;

public record WeatherDto(
        UUID id,
        Instant forecastedAt,
        Instant forecastAt,
        WeatherAPILocation location,
        SkyStatus skyStatus,
        PrecipitationDto precipitation,
        HumidityDto humidity,
        TemperatureDto temperature,
        WindSpeedDto windSpeed
) {
}
