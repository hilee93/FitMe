package com.ootd.fitme.domain.recommendation.controller;

import com.ootd.fitme.domain.recommendation.dto.response.RecommendationDto;
import com.ootd.fitme.domain.recommendation.service.RecommendationService;
import com.ootd.fitme.global.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = RecommendationController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RecommendationController 테스트")
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecommendationService recommendationService;

    @Nested
    @DisplayName("GET /api/recommendations (추천 조회)")
    class GetRecommendationTest {

        @Test
        @DisplayName("[200] 유효한 요청으로 추천 결과와 함께 200 OK 반환")
        void getRecommendation_success_when_validRequest() throws Exception {
            // given
            String userId = "123e4567-e89b-12d3-a456-426614174000";
            UUID weatherId = UUID.randomUUID();

            RecommendationDto responseDto = new RecommendationDto(
                    weatherId,
                    UUID.fromString(userId),
                    Collections.emptyList()
            );

            given(recommendationService.recommendation(UUID.fromString(userId), weatherId)).willReturn(responseDto);

            // when & then
            mockMvc.perform(get("/api/recommendations")
                            .param("weatherId", weatherId.toString())
                            .principal(() -> userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(userId))
                    .andExpect(jsonPath("$.weatherId").value(weatherId.toString()))
                    .andExpect(jsonPath("$.clothes").isArray());

            then(recommendationService).should(times(1)).recommendation(UUID.fromString(userId), weatherId);
        }

        @Test
        @DisplayName("[400] weatherId 파라미터가 누락되면 400 반환")
        void getRecommendation_fail_when_weatherId_missing() throws Exception {
            // given
            String userId = "123e4567-e89b-12d3-a456-426614174000";

            // when & then
            mockMvc.perform(get("/api/recommendations")
                            .principal(() -> userId))
                    .andExpect(status().isBadRequest());

            then(recommendationService).should(never()).recommendation(any(), any());
        }

        @Test
        @DisplayName("[401] 인증 없이 요청하면 401 반환")
        void getRecommendation_fail_when_noPrincipal() throws Exception {
            // given
            UUID weatherId = UUID.randomUUID();

            // when & then
            mockMvc.perform(get("/api/recommendations")
                            .param("weatherId", weatherId.toString()))
                    .andExpect(status().isUnauthorized());

            then(recommendationService).should(never()).recommendation(any(), any());
        }
    }
}
