package com.ootd.fitme.domain.weatherforecast.event;

import java.time.Instant;
import java.util.UUID;

public record WeatherAlertEvent(
        String region_1,
        String region_2,
        String message,
        Instant createdAt
) {
}
