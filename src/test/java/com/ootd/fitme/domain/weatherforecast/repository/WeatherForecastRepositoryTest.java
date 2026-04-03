package com.ootd.fitme.domain.weatherforecast.repository;

import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.region.repository.RegionRepository;
import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;
import com.ootd.fitme.domain.weatherforecast.enums.WindStrengthWord;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import com.ootd.fitme.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
public class WeatherForecastRepositoryTest {
    @Autowired
    private WeatherForecastRepository weatherForecastRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Test
    @DisplayName("findUpcomingByRegionId - baseTime 이후 데이터만 오름차순으로 최대 5건 조회한다")
    void findUpcomingByRegionId_returnsTop5AfterBaseTime() {
        Region region = saveRegion("1111061500");
        Instant baseTime = Instant.parse("2026-04-02T00:00:00Z");

        saveWeather(region, baseTime.minusSeconds(3600), baseTime.minusSeconds(3600));
        saveWeather(region, baseTime.plusSeconds(3600), baseTime.plusSeconds(3600));
        saveWeather(region, baseTime.plusSeconds(7200), baseTime.plusSeconds(7200));
        saveWeather(region, baseTime.plusSeconds(10800), baseTime.plusSeconds(10800));
        saveWeather(region, baseTime.plusSeconds(14400), baseTime.plusSeconds(14400));
        saveWeather(region, baseTime.plusSeconds(18000), baseTime.plusSeconds(18000));
        saveWeather(region, baseTime.plusSeconds(21600), baseTime.plusSeconds(21600));

        List<WeatherForecast> result = weatherForecastRepository.findUpcomingByRegionId(
                region.getId(),
                baseTime,
                PageRequest.of(0, 5)
        );

        assertThat(result).hasSize(5);
        assertThat(result).allMatch(w -> !w.getForecastedAt().isBefore(baseTime));
        assertThat(result).isSortedAccordingTo((a,b) -> a.getForecastedAt().compareTo(b.getForecastedAt()));
        assertThat(result.get(0).getForecastedAt()).isEqualTo(baseTime.plusSeconds(3600));
        assertThat(result.get(4).getForecastedAt()).isEqualTo(baseTime.plusSeconds(18000));
    }

    @Test
    @DisplayName("findUpcomingByRegionId - 다른 region이면 빈 리스트를 반환")
    void findUpcomingByRegionId_returnsEmptyWhenRegionMismatch() {
        Region regionA = saveRegion("1111061500");
        Region regionB = saveRegion("2611054500");
        Instant baseTime = Instant.parse("2026-04-02T00:00:00Z");

        saveWeather(regionA, baseTime.plusSeconds(3600),baseTime.plusSeconds(3600));

        List<WeatherForecast> result = weatherForecastRepository.findUpcomingByRegionId(
                regionB.getId(),
                baseTime,
                PageRequest.of(0, 5)
        );

        assertThat(result).isEmpty();
    }

    private Region saveRegion(String regionCode) {
        return regionRepository.save(
                Region.create(
                        regionCode,
                        "주소-" + regionCode,
                        "시도",
                        "시군구",
                        "읍면동",
                        null,
                        126.9707,
                        37.5841,
                        127,
                        38
                )
        );
    }

    private WeatherForecast saveWeather(Region region,
                                        Instant forecastedAt,
                                        Instant forecastAt) {
        return weatherForecastRepository.save(
                WeatherForecast.create(
                        forecastedAt,
                        forecastAt,
                        SkyStatus.CLEAR,
                        PrecipitationType.NONE,
                        0.0,
                        0.0,
                        40.0,
                        -5.0,
                        10.0,
                        1.0,
                        1.0,
                        5.0,
                        2.0,
                        WindStrengthWord.WEAK,
                        region
                )
        );
    }
}
