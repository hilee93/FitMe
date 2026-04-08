package com.ootd.fitme.domain.weatherforecast.controller;

import com.ootd.fitme.domain.weatherforecast.dto.request.WeatherRequest;
import com.ootd.fitme.domain.weatherforecast.dto.response.WeatherAPILocation;
import com.ootd.fitme.domain.weatherforecast.dto.response.WeatherDto;
import com.ootd.fitme.domain.weatherforecast.service.WeatherForecastService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/weathers")
@RequiredArgsConstructor
public class WeatherForecastController {
    private final WeatherForecastService weatherForecastService;

    @GetMapping
    public ResponseEntity<List<WeatherDto>> getWeathers(@Valid WeatherRequest weatherRequest) {
        return ResponseEntity.ok(weatherForecastService.getWeathers(
                weatherRequest.longitude(), weatherRequest.latitude()));
    }

    @GetMapping("/location")
    public ResponseEntity<WeatherAPILocation> getLocation(@Valid WeatherRequest weatherRequest) {
        return ResponseEntity.ok(weatherForecastService.getWeatherLocation(
                weatherRequest.longitude(), weatherRequest.latitude()));
    }
}
