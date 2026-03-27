package com.ootd.fitme.domain.weatherforecast.repository;

import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WeatherForecastRepository extends JpaRepository<WeatherForecast, UUID> {
}
