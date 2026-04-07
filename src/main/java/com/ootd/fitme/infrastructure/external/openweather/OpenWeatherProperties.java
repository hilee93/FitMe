package com.ootd.fitme.infrastructure.external.openweather;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external.openweather")
public record OpenWeatherProperties(
        String restApiKey,
        String baseUrl,
        String forecastPath
) {
}
