package com.ootd.fitme.domain.weatherforecast.service;

import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.region.repository.RegionRepository;
import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;
import com.ootd.fitme.domain.weatherforecast.enums.WindStrengthWord;
import com.ootd.fitme.domain.weatherforecast.repository.WeatherForecastRepository;
import com.ootd.fitme.infrastructure.external.openweather.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WeatherForecastCollectServiceUnitTest {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private WeatherForecastRepository weatherForecastRepository;

    @Mock
    private OpenWeatherClient openWeatherClient;

    @InjectMocks
    private WeatherForecastCollectService collectService;

    @Test
    @DisplayName("첫 수집이면 오늘 포함 5일을 저장 대상으로 사용")
    void collectAndStoreAllRegion_firstCollection_includesToday() {
        UUID regionId = UUID.randomUUID();
        Region region = mockRegion(regionId, 126.9707, 37.5841);
        LocalDate today = LocalDate.now(KST);

        given(regionRepository.findAll()).willReturn(List.of(region));
        given(weatherForecastRepository.existsByRegionId(regionId)).willReturn(false);
        given(openWeatherClient.fetch5Day3HourForecast(126.9707, 37.5841))
                .willReturn(forecastItems(today, 6));
        given(weatherForecastRepository.findLatestBeforeForecastAt(eq(regionId), any(Instant.class)))
                .willReturn(Optional.empty());
        given(weatherForecastRepository.findByRegionIdAndForecastAt(eq(regionId), any(Instant.class)))
                .willReturn(Optional.empty());

        collectService.collectAndStoreAllRegions();

        ArgumentCaptor<Instant> forecastAtCaptor = ArgumentCaptor.forClass(Instant.class);
        then(weatherForecastRepository).should(times(5))
                .findByRegionIdAndForecastAt(eq(regionId), forecastAtCaptor.capture());
        then(weatherForecastRepository).should(times(5)).save(any(WeatherForecast.class));


        List<LocalDate> actualDates = forecastAtCaptor.getAllValues().stream()
                .map(i -> i.atZone(KST).toLocalDate())
                .toList();

        List<LocalDate> expectedDates = IntStream.range(0, 5)
                .mapToObj(today::plusDays)
                .toList();

        assertThat(actualDates).containsExactlyElementsOf(expectedDates);
    }

    @Test
    @DisplayName("기존 이력이 있으면 내일부터 5일을 저장 대상으로 사용")
    void collectAndStoreAllRegions_withHistory_startsFromTomorrow() {
        UUID regionId = UUID.randomUUID();
        Region region = mockRegion(regionId, 126.9707, 37.5841);
        LocalDate today = LocalDate.now(KST);

        given(regionRepository.findAll()).willReturn(List.of(region));
        given(weatherForecastRepository.existsByRegionId(regionId)).willReturn(true);
        given(openWeatherClient.fetch5Day3HourForecast(126.9707, 37.5841))
                .willReturn(forecastItems(today, 7));
        given(weatherForecastRepository.findLatestBeforeForecastAt(eq(regionId), any(Instant.class)))
                .willReturn(Optional.empty());
        given(weatherForecastRepository.findByRegionIdAndForecastAt(eq(regionId), any(Instant.class)))
                .willReturn(Optional.empty());

        collectService.collectAndStoreAllRegions();

        ArgumentCaptor<Instant> forecastAtCaptor = ArgumentCaptor.forClass(Instant.class);
        then(weatherForecastRepository).should(times(5))
                .findByRegionIdAndForecastAt(eq(regionId), forecastAtCaptor.capture());

        List<LocalDate> actualDates = forecastAtCaptor.getAllValues().stream()
                .map(i -> i.atZone(KST).toLocalDate())
                .toList();

        List<LocalDate> expectedDates = IntStream.range(1, 6)
                .mapToObj(today::plusDays)
                .toList();

        assertThat(actualDates).containsExactlyElementsOf(expectedDates);
    }

    @Test
    @DisplayName("동일 forecastAt이 있으면 save가 아닌 update 경로를 탄다")
    void collectAndStoreAllRegions_existingForecast_updatesWithoutInsert() {
        UUID regionId = UUID.randomUUID();
        Region region = mockRegion(regionId, 126.9707, 37.5841);
        LocalDate today = LocalDate.now(KST);

        ForecastItem item = forecastItem(today, 12, 23.0, 65.0, 801, 0.2, 0.0, 0.0, 3.5);
        Instant forecastAt = Instant.ofEpochSecond(item.dt());

        WeatherForecast prev = WeatherForecast.create(
                Instant.now().minusSeconds(7200),
                forecastAt.minusSeconds(10800),
                SkyStatus.CLEAR,
                PrecipitationType.NONE,
                0.0,
                0.0,
                60.0,
                0.0,
                20.0,
                0.0,
                19.0,
                21.0,
                2.0,
                WindStrengthWord.WEAK,
                region
        );

        WeatherForecast existing = WeatherForecast.create(
                Instant.now().minusSeconds(7200),
                forecastAt,
                SkyStatus.CLEAR,
                PrecipitationType.NONE,
                0.0,
                0.0,
                40.0,
                0.0,
                10.0,
                0.0,
                9.0,
                11.0,
                1.0,
                WindStrengthWord.WEAK,
                region
        );

        Instant beforeUpdateForecastedAt = existing.getForecastedAt();

        given(regionRepository.findAll()).willReturn(List.of(region));
        given(weatherForecastRepository.existsByRegionId(regionId)).willReturn(false);
        given(openWeatherClient.fetch5Day3HourForecast(126.9707, 37.5841))
                .willReturn(List.of(item));
        given(weatherForecastRepository.findLatestBeforeForecastAt(regionId, forecastAt))
                .willReturn(Optional.of(prev));
        given(weatherForecastRepository.findByRegionIdAndForecastAt(regionId, forecastAt))
                .willReturn(Optional.of(existing));

        collectService.collectAndStoreAllRegions();

        then(weatherForecastRepository).should(never()).save(any(WeatherForecast.class));
        then(weatherForecastRepository).should().findByRegionIdAndForecastAt(regionId, forecastAt);

        assertThat(existing.getSkyStatus()).isEqualTo(SkyStatus.MOSTLY_CLOUDY);
        assertThat(existing.getPrecipitationType()).isEqualTo(PrecipitationType.NONE);
        assertThat(existing.getPrecipitationProbability()).isEqualTo(20.0);
        assertThat(existing.getHumidityCurrent()).isEqualTo(65.0);
        assertThat(existing.getHumidityComparedToDayBefore()).isEqualTo(5.0);
        assertThat(existing.getTemperatureCurrent()).isEqualTo(23.0);
        assertThat(existing.getTemperatureComparedToDayBefore()).isEqualTo(3.0);
        assertThat(existing.getWindStrengthWord()).isEqualTo(WindStrengthWord.WEAK);
        assertThat(existing.getForecastedAt()).isAfter(beforeUpdateForecastedAt);
    }

    private Region mockRegion(UUID id, double longitude, double latitude) {
        Region region = mock(Region.class);
        given(region.getId()).willReturn(id);
        given(region.getLongitude()).willReturn(longitude);
        given(region.getLatitude()).willReturn(latitude);
        return region;
    }

    private List<ForecastItem> forecastItems(LocalDate startDate, int dayCount) {
        return IntStream.range(0, dayCount)
                .mapToObj(i -> forecastItem(
                        startDate.plusDays(i),
                        12,
                        20.0 + i,
                        50.0 + i,
                        800,
                        0.1,
                        0.0,
                        0.0,
                        2.0
                ))
                .toList();
    }

    private ForecastItem forecastItem(
            LocalDate date,
            int hour,
            double temp,
            double humidity,
            int weatherCode,
            double pop,
            double rain3h,
            double snow3h,
            double windSpeed
    ) {
        long epoch = date.atTime(hour, 0).atZone(KST).toEpochSecond();
        return new ForecastItem(
                epoch,
                new Main(temp, temp - 1.0, temp + 1.0, humidity),
                List.of(new Weather(weatherCode, "main", "desc")),
                new Wind(windSpeed),
                pop,
                new Rain(rain3h),
                new Snow(snow3h)
        );
    }
}
