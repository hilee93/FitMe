package com.ootd.fitme.domain.weatherforecast.service;

import com.ootd.fitme.domain.weatherforecast.dto.response.WeatherAPILocation;
import com.ootd.fitme.domain.weatherforecast.dto.response.WeatherDto;

import java.util.List;

public interface WeatherForecastService {
    List<WeatherDto> getWeathers(double longitude, double latitude);
    WeatherAPILocation getWeatherLocation(double longitude, double latitude);
}
