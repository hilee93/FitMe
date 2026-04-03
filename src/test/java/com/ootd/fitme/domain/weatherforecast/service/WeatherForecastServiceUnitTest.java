package com.ootd.fitme.domain.weatherforecast.service;

import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.region.service.RegionService;
import com.ootd.fitme.domain.weatherforecast.dto.response.*;
import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;
import com.ootd.fitme.domain.weatherforecast.enums.WindStrengthWord;
import com.ootd.fitme.domain.weatherforecast.mapper.WeatherForecastMapper;
import com.ootd.fitme.domain.weatherforecast.repository.WeatherForecastRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WeatherForecastServiceUnitTest {
    @Mock
    private WeatherForecastRepository weatherForecastRepository;

    @Mock
    private WeatherForecastMapper weatherForecastMapper;

    @Mock
    private RegionService regionService;

    @InjectMocks
    private WeatherForecastServiceImpl weatherForecastService;

    @Nested
    @DisplayName("getWeathers")
    class GetWeathersTest {
        @Test
        @DisplayName("성공 - region 조회 후 예보 5건 조회/매핑을 수행한다")
        void getWeathers_success() {
            UUID regionId = UUID.randomUUID();
            Region region = mock(Region.class);
            given(region.getId()).willReturn(regionId);

            WeatherForecast wf1 = mock(WeatherForecast.class);
            WeatherForecast wf2 = mock(WeatherForecast.class);

            WeatherDto dto1 = weatherDto();
            WeatherDto dto2 = weatherDto();

            given(regionService.resolveAndUpsert(126.9707, 37.5841)).willReturn(region);
            given(weatherForecastRepository.findUpcomingByRegionId(eq(regionId), any(Instant.class), any(Pageable.class)))
                    .willReturn(List.of(wf1, wf2));
            given(weatherForecastMapper.toDto(wf1)).willReturn(dto1);
            given(weatherForecastMapper.toDto(wf2)).willReturn(dto2);

            List<WeatherDto> result = weatherForecastService.getWeathers(126.9707, 37.5841);

            assertThat(result).containsExactly(dto1, dto2);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(weatherForecastRepository).findUpcomingByRegionId(eq(regionId), any(Instant.class), pageableCaptor.capture());
            assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(0);
            assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
        }

        @Test
        @DisplayName("성공 - 조회 결과가 없으면 빈 리스트를 반환")
        void getWeathers_empty() {
            UUID regionId = UUID.randomUUID();
            Region region = mock(Region.class);
            given(region.getId()).willReturn(regionId);

            given(regionService.resolveAndUpsert(126.9707, 37.5841)).willReturn(region);
            given(weatherForecastRepository.findUpcomingByRegionId(eq(regionId), any(Instant.class), any(Pageable.class)))
                    .willReturn(List.of());

            List<WeatherDto> result = weatherForecastService.getWeathers(126.9707, 37.5841);

            assertThat(result).isEmpty();
            verify(weatherForecastMapper, never()).toDto(any());
        }
    }

    @Test
    @DisplayName("getWeatherLocation - regionService 결과를 그대로 반환")
    void getWeatherLocation_success() {
        WeatherAPILocation location = new WeatherAPILocation(
                37.5841,
                126.9707,
                127,
                38,
                List.of("서울", "종로구", "청운효자동")
        );

        given(regionService.resolveLocation(126.9707, 37.5841)).willReturn(location);

        WeatherAPILocation result = weatherForecastService.getWeatherLocation(126.9707, 37.5841);

        assertThat(result).isEqualTo(location);
    }

    private WeatherDto weatherDto() {
        return new WeatherDto(
                UUID.randomUUID(),
                Instant.parse("2026-04-02T00:00:00Z"),
                Instant.parse("2026-04-02T03:00:00Z"),
                new WeatherAPILocation(
                        37.5841,
                        126.9707,
                        127,
                        38,
                        List.of("서울", "종로구", "청운효자동")),
                SkyStatus.CLEAR,
                new PrecipitationDto(PrecipitationType.NONE, 0.0, 0.0),
                new HumidityDto(40.0, -5.0),
                new TemperatureDto(10.0, 2.0, 6.0, 15.0),
                new WindSpeedDto(2.5, WindStrengthWord.WEAK)
        );
    }
}
