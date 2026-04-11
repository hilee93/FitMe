package com.ootd.fitme.domain.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.profile.dto.request.ProfileUpdateRequest;
import com.ootd.fitme.domain.profile.dto.response.ProfileDto;
import com.ootd.fitme.domain.profile.enums.Gender;
import com.ootd.fitme.domain.profile.service.ProfileService;
import com.ootd.fitme.domain.weatherforecast.dto.response.WeatherAPILocation;
import com.ootd.fitme.global.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = ProfileController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
public class ProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProfileService profileService;

    @Nested
    @DisplayName("GET /api/users/{userId}/profiles")
    class GetProfile {
        @Test
        @DisplayName("성공 - 프로필 조회 시 200과 ProfileDto 반환")
        void getProfile_success() throws Exception {
            UUID userId = UUID.randomUUID();
            ProfileDto dto = profileDto(userId);

            given(profileService.getProfile(userId)).willReturn(dto);

            mockMvc.perform(get("/api/users/{userId}/profiles", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(userId.toString()))
                    .andExpect(jsonPath("$.name").value("tester"))
                    .andExpect(jsonPath("$.gender").value("OTHER"))
                    .andExpect(jsonPath("$.location.latitude").value(37.5665))
                    .andExpect(jsonPath("$.location.longitude").value(126.9780))
                    .andExpect(jsonPath("$.temperatureSensitivity").value(3));

            then(profileService).should().getProfile(userId);
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{userId}/profiles")
    class UpdateProfile {
        @Test
        @DisplayName("성공 - multipart 요청으로 프로필 수정 시 200 반환")
        void updateProfile_success() throws Exception {
            UUID userId = UUID.randomUUID();

            ProfileUpdateRequest request = new ProfileUpdateRequest(
                    "tester",
                    Gender.OTHER,
                    LocalDate.of(2000, 1, 1),
                    new WeatherAPILocation(37.5665, 126.9780, null, null, List.of()),
                    3
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            ProfileDto response = profileDto(userId);
            given(profileService.updateProfile(eq(userId), any(ProfileUpdateRequest.class), any()))
                    .willReturn(response);

            mockMvc.perform(multipart("/api/users/{userId}/profiles", userId)
                            .file(requestPart)
                            .with(req -> {
                                req.setMethod(HttpMethod.PATCH.name());
                                return req;
                            }))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(userId.toString()))
                    .andExpect(jsonPath("$.name").value("tester"))
                    .andExpect(jsonPath("$.location.locationNames[0]").value("서울특별시"));

            then(profileService).should().updateProfile(eq(userId), any(ProfileUpdateRequest.class), any());
        }

        @Test
        @DisplayName("실패 - 온도 민감도 범위(1~5) 위반 시 400 반환")
        void updateProfile_invalidTemperatureSensitivity_return400() throws Exception {
            UUID userId = UUID.randomUUID();

            ProfileUpdateRequest invalid = new ProfileUpdateRequest(
                    "tester",
                    Gender.OTHER,
                    LocalDate.of(2000, 1, 1),
                    new WeatherAPILocation(37.5665, 126.9780, null, null, List.of()),
                    6
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(invalid)
            );

            mockMvc.perform(multipart("/api/users/{userId}/profiles", userId)
                            .file(requestPart)
                            .with(req -> {
                                req.setMethod(HttpMethod.PATCH.name());
                                return req;
                            }))
                    .andExpect(status().isBadRequest());

            then(profileService).should(never()).updateProfile(any(), any(), any());
        }
    }

    private ProfileDto profileDto(UUID userId) {
        return new ProfileDto(
                userId,
                "tester",
                Gender.OTHER,
                LocalDate.of(2000, 1, 1),
                new WeatherAPILocation(
                        37.5665,
                        126.9780,
                        127,
                        38,
                        List.of("서울특별시", "중구", "소공동")
                ),
                3,
                "/uploads/profile/tester.png"
        );
    }
}
