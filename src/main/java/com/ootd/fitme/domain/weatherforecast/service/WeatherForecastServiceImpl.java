package com.ootd.fitme.domain.weatherforecast.service;

import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.region.service.RegionService;
import com.ootd.fitme.domain.weatherforecast.dto.response.WeatherAPILocation;
import com.ootd.fitme.domain.weatherforecast.dto.response.WeatherDto;
import com.ootd.fitme.domain.weatherforecast.mapper.WeatherForecastMapper;
import com.ootd.fitme.domain.weatherforecast.repository.WeatherForecastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherForecastServiceImpl implements WeatherForecastService {

    private final WeatherForecastRepository weatherForecastRepository;
    private final RegionService regionService;
    private final WeatherForecastMapper weatherForecastMapper;

    @Override
    @Transactional
    public List<WeatherDto> getWeathers(double longitude, double latitude) {
        Region region = regionService.resolveAndUpsert(longitude, latitude);

        return weatherForecastRepository
                .findUpcomingByRegionId(region.getId(), Instant.now(), PageRequest.of(0, 5))
                .stream()
                .map(weatherForecastMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public WeatherAPILocation getWeatherLocation(double longitude, double latitude) {
        return regionService.resolveLocation(longitude, latitude);
    }
}
