package com.ootd.fitme.domain.weatherforecast.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WeatherAlertEvent(
        List<UUID> receiverIds,
        String region1DepthName,
        String region2DepthName,
        String message,
        Instant createdAt
) {
}
