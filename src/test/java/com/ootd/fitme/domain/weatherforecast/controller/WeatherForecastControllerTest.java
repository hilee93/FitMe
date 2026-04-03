package com.ootd.fitme.domain.weatherforecast.controller;

import com.ootd.fitme.domain.weatherforecast.dto.response.*;
import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;
import com.ootd.fitme.domain.weatherforecast.enums.WindStrengthWord;
import com.ootd.fitme.domain.weatherforecast.service.WeatherForecastService;
import com.ootd.fitme.global.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = WeatherForecastController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
public class WeatherForecastControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherForecastService weatherForecastService;

    @Nested
    @DisplayName("GET /api/weathers")
    class GetWeathersTest {
        @Test
        @DisplayName("성공 - 유효한 좌표면 200과 날씨 목록을 반환")
        void getWeathers_success() throws Exception {
            WeatherDto dto = weatherDto();
            given(weatherForecastService.getWeathers(126.9707, 37.5841))
                    .willReturn(List.of(dto));

            mockMvc.perform(get("/api/weathers")
                    .param("longitude", "126.9707")
                    .param("latitude", "37.5841"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(dto.id().toString()))
                    .andExpect(jsonPath("$[0].skyStatus").value("CLEAR"))
                    .andExpect(jsonPath("$[0].location.x").value(127));

            then(weatherForecastService).should().getWeathers(126.9707, 37.5841);
        }

        @Test
        @DisplayName("실패 - latitude 범위가 유효하지 않으면 400을 반환")
        void getWeathers_invalidLatitude_return400() throws Exception {
            mockMvc.perform(get("/api/weathers")
                    .param("longitude", "126.9707")
                    .param("latitude", "100.0"))
                    .andExpect(status().isBadRequest());

            then(weatherForecastService).should(never()).getWeathers(anyDouble(), anyDouble());
        }

        @Test
        @DisplayName("실패 - longitude가 누락되면 400을 반환")
        void getWeathers_missingLongitude_return400() throws Exception {
            mockMvc.perform(get("/api/weathers")
                    .param("latitude", "37.5841"))
                    .andExpect(status().isBadRequest());

            then(weatherForecastService).should(never()).getWeathers(anyDouble(), anyDouble());
        }
    }

    @Nested
    @DisplayName("GET /api/weathers/location")
    class GetWeathersLocationTest {
        @Test
        @DisplayName("성공 - 유효한 좌표면 200과 위치 정보를 반환")
        void getLocation_success() throws Exception {
            WeatherAPILocation location = new WeatherAPILocation(
                    37.5841,
                    126.9707,
                    127,
                    38,
                    List.of("서울", "종로구", "청운효자동")
            );

            given(weatherForecastService.getWeatherLocation(126.9707, 37.5841))
                    .willReturn(location);

            mockMvc.perform(get("/api/weathers/location")
                    .param("longitude", "126.9707")
                    .param("latitude", "37.5841"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.x").value(127))
                    .andExpect(jsonPath("$.locationNames[0]").value("서울"));

            then(weatherForecastService).should().getWeatherLocation(126.9707, 37.5841);

        }
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
                new PrecipitationDto(
                        PrecipitationType.NONE,
                        0.0,
                        0.0),
                new HumidityDto(40.0, -5.0),
                new TemperatureDto(
                        10.0,
                        2.0,
                        6.0,
                        15.0),
                new WindSpeedDto(2.5, WindStrengthWord.WEAK)
        );
    }
}
