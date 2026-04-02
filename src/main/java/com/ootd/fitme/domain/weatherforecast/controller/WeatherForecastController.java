package com.ootd.fitme.domain.weatherforecast.controller;

import com.ootd.fitme.domain.weatherforecast.service.WeatherForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weathers")
@RequiredArgsConstructor
public class WeatherForecastController {
    private final WeatherForecastService weatherForecastService;

    @GetMapping
    public void getWeathers() {
    }

    @GetMapping("/location")
    public void getLocation() {
    }
}
