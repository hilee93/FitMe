package com.ootd.fitme.infrastructure.external.openweather;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

// openweather 5day/3hour 응답
@Component
@RequiredArgsConstructor
public class OpenWeatherClient {
    private final RestClient restClient;
    private final OpenWeatherProperties properties;

    public List<ForecastItem> fetch5Day3HourForecast(
            double longitude, double latitude
    ) {
        URI uri = UriComponentsBuilder.fromUriString(properties.baseUrl())
                .path(properties.forecastPath())
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("appid", properties.restApiKey())
                .queryParam("units", "metric")
                .build(true)
                .toUri();

        OpenWeatherForecastResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(OpenWeatherForecastResponse.class);

        if (response == null || response.list() == null) {
            throw new IllegalStateException("OpenWeather forecast response is empty");
        }

        return response.list();
    }
}
