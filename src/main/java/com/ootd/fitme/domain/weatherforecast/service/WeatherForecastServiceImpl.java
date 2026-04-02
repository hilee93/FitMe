package com.ootd.fitme.domain.weatherforecast.service;

import com.ootd.fitme.domain.weatherforecast.repository.WeatherForecastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherForecastServiceImpl implements WeatherForecastService {

    private final WeatherForecastRepository weatherForecastRepository;
}
