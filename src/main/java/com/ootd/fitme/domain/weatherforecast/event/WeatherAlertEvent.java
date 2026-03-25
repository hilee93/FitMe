package com.ootd.fitme.domain.weatherforecast.event;

import java.time.Instant;
import java.util.UUID;

public record WeatherAlertEvent(
        String message,
        Instant createdAt
) {
}
