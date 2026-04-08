package com.ootd.fitme.domain.weatherforecast.mapper;

import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.weatherforecast.dto.response.*;
import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
public class WeatherForecastMapper {
    public WeatherDto toDto(WeatherForecast weather) {
        Region r = weather.getRegion();
        return new WeatherDto(
                weather.getId(),
                weather.getForecastedAt(),
                weather.getForecastAt(),
                new WeatherAPILocation(
                        r.getLatitude(),
                        r.getLongitude(),
                        r.getX(),
                        r.getY(),
                        locationNames(r)),
                weather.getSkyStatus(),
                new PrecipitationDto(
                        weather.getPrecipitationType(),
                        weather.getPrecipitationAmount(),
                        weather.getPrecipitationProbability()),
                new HumidityDto(weather.getHumidityCurrent(), weather.getHumidityComparedToDayBefore()),
                new TemperatureDto(
                        weather.getTemperatureCurrent(),
                        weather.getTemperatureComparedToDayBefore(),
                        weather.getTemperatureMin(),
                        weather.getTemperatureMax()),
                new WindSpeedDto(weather.getWindSpeed(), weather.getWindStrengthWord())
        );
    }
    private List<String> locationNames(Region r) {
        return Stream.of(
                r.getRegion1depthName(),
                r.getRegion2depthName(),
                r.getRegion3depthName(),
                r.getRegion4depthName()
                )
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
