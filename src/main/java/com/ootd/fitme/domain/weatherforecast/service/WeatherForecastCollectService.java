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

        for (LocalDate date : targetDates) {
            ForecastItem item = picked.get(date);

            if (item != null) {
                upsert(region, collectedAt, item);
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
        int da = Math.abs(toKstHour(a.dt()) - PIVOT_HOUR);
        int db = Math.abs(toKstHour(b.dt()) - PIVOT_HOUR);
        return da <= db ? a : b;
    }

    private void upsert(Region region, Instant collectedAt, ForecastItem item) {
        Instant forecastAt = Instant.ofEpochSecond(item.dt());

        double humidityCurrent = nvl(item.main() != null ? item.main().humidity() : null);
        double temperatureCurrent = nvl(item.main() != null ? item.main().temp() : null);

        Delta delta = weatherForecastRepository.findLatestBeforeForecastAt(region.getId(), forecastAt)
                .map(prev -> new Delta(
                        humidityCurrent - nvl(prev.getHumidityCurrent()),
                        temperatureCurrent - nvl(prev.getTemperatureCurrent())
                ))
                .orElse(new Delta(0.0, 0.0));

        SkyStatus skyStatus = toSkyStatus(weatherCode(item));
        PrecipitationType precipitationType = toPrecipitationType(weatherCode(item));
        double precipitationAmount = precipitationAmount(item);
        double precipitationProbability = nvl(item.pop()) * 100.0;
        double temperatureMin = nvl(item.main() != null ? item.main().tempMin() : null);
        double temperatureMax = nvl(item.main() != null ? item.main().tempMax() : null);
        double windSpeed = nvl(item.wind() != null ? item.wind().speed() : null);
        WindStrengthWord windStrengthWord = toWindStrength(windSpeed);

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

    private int toKstHour(long epochSecond) {
        return Instant.ofEpochSecond(epochSecond).atZone(KST).getHour();
    }

    private int weatherCode(ForecastItem item) {
        if (item.weather() == null || item.weather().isEmpty() || item.weather().get(0).id() == null) {
            return 800;
        }
        return item.weather().get(0).id();
    }

    private SkyStatus toSkyStatus(int code) {
        if (code == 800) {
            return SkyStatus.CLEAR;
        }

        if (code == 801 || code == 802) {
            return SkyStatus.MOSTLY_CLOUDY;
        }

        return SkyStatus.CLOUDY;
    }

    private PrecipitationType toPrecipitationType(int code) {
        if (code >= 200 && code <= 400) {
            return PrecipitationType.SHOWER;
        }

        if (code >= 500 && code < 600) {
            return PrecipitationType.RAIN;
        }

        if (code == 611 || code == 612 || code == 613 || code == 615 || code == 616) {
            return PrecipitationType.RAIN_SNOW;
        }

        if (code >= 600 && code < 700) {
            return PrecipitationType.SNOW;
        }

        return PrecipitationType.NONE;
    }

    private WindStrengthWord toWindStrength(double speed) {
        if (speed < 4.0) {
            return WindStrengthWord.WEAK;
        }

        if (speed < 9.0) {
            return WindStrengthWord.MODERATE;
        }

        return WindStrengthWord.STRONG;
    }

    private double precipitationAmount(ForecastItem item) {
        double rain = item.rain() != null ? nvl(item.rain().volume3h()) : 0.0;
        double snow = item.snow() != null ? nvl(item.snow().volume3h()) : 0.0;
        return rain + snow;
    }

    // null value logic
    private double nvl(Double value) {
        return value != null ? value : 0.0;
    }

    private record Delta(double humidityDiff, double temperatureDiff) {
    }
}
