package com.ootd.fitme.infrastructure.external.openweather;

import java.util.List;

public record OpenWeatherForecastResponse(
        List<ForecastItem> list
) {
}
