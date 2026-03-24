package com.ootd.fitme.domain.notiication.event;

import java.util.UUID;

public record WeatherAlertEvent(
        UUID userId,
        String message
) {
}
