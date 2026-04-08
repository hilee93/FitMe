package com.ootd.fitme.domain.weatherforecast.service;

import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.region.service.RegionService;
import com.ootd.fitme.domain.weatherforecast.dto.response.WeatherAPILocation;
import com.ootd.fitme.domain.weatherforecast.dto.response.WeatherDto;
import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import com.ootd.fitme.domain.weatherforecast.mapper.WeatherForecastMapper;
import com.ootd.fitme.domain.weatherforecast.repository.WeatherForecastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WeatherForecastServiceImpl implements WeatherForecastService {

    private final WeatherForecastRepository weatherForecastRepository;
    private final RegionService regionService;
    private final WeatherForecastMapper weatherForecastMapper;
    private final WeatherForecastCollectService weatherForecastCollectService;

    @Override
    @Transactional
    public List<WeatherDto> getWeathers(double longitude, double latitude) {
        Region region = regionService.resolveAndUpsert(longitude, latitude);

        List<WeatherForecast> forecasts = findUpcoming(region.getId());

        if (forecasts.isEmpty()) {
            weatherForecastCollectService.collectAndStoreRegion(region);
            forecasts = findUpcoming(region.getId());
        }

        return forecasts.stream()
                .map(weatherForecastMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public WeatherAPILocation getWeatherLocation(double longitude, double latitude) {
        return regionService.resolveLocation(longitude, latitude);
    }

    private List<WeatherForecast> findUpcoming(UUID regionId) {
        return weatherForecastRepository.findUpcomingByRegionId(
                regionId,
                Instant.now(),
                PageRequest.of(0, 5)
        );
    }
}
