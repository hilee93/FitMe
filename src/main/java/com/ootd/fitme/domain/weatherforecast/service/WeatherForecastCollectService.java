package com.ootd.fitme.domain.weatherforecast.service;

import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.region.repository.RegionRepository;
import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;
import com.ootd.fitme.domain.weatherforecast.enums.WindStrengthWord;
import com.ootd.fitme.domain.weatherforecast.repository.WeatherForecastRepository;
import com.ootd.fitme.infrastructure.external.openweather.ForecastItem;
import com.ootd.fitme.infrastructure.external.openweather.OpenWeatherClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
public class WeatherForecastCollectService {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int DAILY_COUNT = 5;
    private static final int PIVOT_HOUR = 12;

    private final RegionRepository regionRepository;
    private final WeatherForecastRepository weatherForecastRepository;
    private final OpenWeatherClient openWeatherClient;
    private final WeatherAlertPublishService weatherAlertPublishService;

    public void collectAndStoreAllRegions() {
        List<Region> regions = regionRepository.findAll();
        for (Region region : regions) {
            collectAndStoreRegion(region);
        }
    }

    public void collectAndStoreRegion(Region region) {
        Instant collectedAt = Instant.now();
        boolean hasHistory = weatherForecastRepository.existsByRegionId(region.getId());
        LocalDate startDate = LocalDate.now(KST).plusDays(hasHistory ? 1 : 0);

        List<LocalDate> targetDates = IntStream.range(0, DAILY_COUNT)
                .mapToObj(startDate::plusDays)
                .toList();

        List<ForecastItem> raw = openWeatherClient.fetch5Day3HourForecast(
                region.getLongitude(), region.getLatitude()
        );

        Map<LocalDate, ForecastItem> picked = pickDailyRepresentative(raw, targetDates);
        Map<LocalDate, DailyTemperatureStat> dailyTemperatureStats =
                DailyTemperatureAggregator.aggregate(raw, targetDates);

        for (LocalDate date : targetDates) {
            ForecastItem item = picked.get(date);
            DailyTemperatureStat dailyTemp = dailyTemperatureStats.get(date);

            if (item != null && dailyTemp != null) {
                upsert(region, collectedAt, item, dailyTemp);
            }
        }
    }

    // 하루 데이터 중 대표 1개 선택
    private Map<LocalDate, ForecastItem> pickDailyRepresentative(
            List<ForecastItem> items,
            List<LocalDate> targetDates
    ) {
        Set<LocalDate> targetSet = new HashSet<>(targetDates);

        return items.stream()
                .filter(i -> i.dt() != null)
                .filter(i -> targetSet.contains(toKstDate(i.dt())))
                .collect(Collectors.toMap(
                        i -> toKstDate(i.dt()),
                        Function.identity(),
                        this::chooseCloserToNoon
                ));
    }

    private ForecastItem chooseCloserToNoon(ForecastItem a, ForecastItem b) {
        int hourA = Instant.ofEpochSecond(a.dt()).atZone(KST).getHour();
        int hourB = Instant.ofEpochSecond(b.dt()).atZone(KST).getHour();

        int diffA = Math.abs(hourA - PIVOT_HOUR);
        int diffB = Math.abs(hourB - PIVOT_HOUR);

        return diffA <= diffB ? a : b;
    }

    private void upsert(Region region, Instant collectedAt, ForecastItem item, DailyTemperatureStat dailyTemp) {
        Instant forecastAt = Instant.ofEpochSecond(item.dt());

        double humidityCurrent = WeatherConditionMapper.valueOrZero(
                item.main() != null ? item.main().humidity() : null);
        double temperatureCurrent = dailyTemp.average();

        Delta delta = weatherForecastRepository.findLatestBeforeForecastAt(region.getId(), forecastAt)
                .map(prev -> new Delta(
                        humidityCurrent - WeatherConditionMapper.valueOrZero(prev.getHumidityCurrent()),
                        temperatureCurrent - WeatherConditionMapper.valueOrZero(prev.getTemperatureCurrent())
                ))
                .orElse(new Delta(0.0, 0.0));

        SkyStatus skyStatus = WeatherConditionMapper.toSkyStatus(item);
        PrecipitationType precipitationType = WeatherConditionMapper.toPrecipitationType(item);
        double precipitationAmount = WeatherConditionMapper.precipitationAmount(item);
        double precipitationProbability = WeatherConditionMapper.valueOrZero(item.pop()) * 100.0;
        double temperatureMin = dailyTemp.minimum();
        double temperatureMax = dailyTemp.maximum();
        double windSpeed = WeatherConditionMapper.valueOrZero(
                item.wind() != null ? item.wind().speed() : null);
        WindStrengthWord windStrengthWord = WeatherConditionMapper.toWindStrength(windSpeed);

        Optional<WeatherForecast> existingOpt = weatherForecastRepository.findByRegionIdAndForecastAt(region.getId(), forecastAt);
        PrecipitationType previousPrecipitationType = existingOpt.map(WeatherForecast::getPrecipitationType)
                .orElse(null);

        WeatherForecast current;

        if (existingOpt.isPresent()) {
            WeatherForecast existing = existingOpt.get();
            existing.updateFromCollection(
                    collectedAt,
                    skyStatus,
                    precipitationType,
                    precipitationAmount,
                    precipitationProbability,
                    humidityCurrent,
                    delta.humidityDiff(),
                    temperatureCurrent,
                    delta.temperatureDiff(),
                    temperatureMin,
                    temperatureMax,
                    windSpeed,
                    windStrengthWord
            );
            current = existing;
        } else {
            current = weatherForecastRepository.save(WeatherForecast.create(
                    collectedAt,
                    forecastAt,
                    skyStatus,
                    precipitationType,
                    precipitationAmount,
                    precipitationProbability,
                    humidityCurrent,
                    delta.humidityDiff(),
                    temperatureCurrent,
                    delta.temperatureDiff(),
                    temperatureMin,
                    temperatureMax,
                    windSpeed,
                    windStrengthWord,
                    region
            ));
        }

        weatherAlertPublishService.publishIfNeeded(region, previousPrecipitationType, current);
    }

    private LocalDate toKstDate(long epochSecond) {
        return Instant.ofEpochSecond(epochSecond).atZone(KST).toLocalDate();
    }

    private record Delta(double humidityDiff, double temperatureDiff) {
    }
}
