package com.ootd.fitme.domain.recommendation.controller;

import com.ootd.fitme.domain.recommendation.dto.response.RecommendationDto;
import com.ootd.fitme.domain.recommendation.service.RecommendationService;
import com.ootd.fitme.global.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
        @DisplayName("[200] 유효한 요청이면 추천 결과와 함께 200 OK를 반환")
        void getRecommendation_success_when_valid_request() throws Exception {
            // given
            UUID userId = UUID.randomUUID();
            UUID weatherId = UUID.randomUUID();

            RecommendationDto responseDto = new RecommendationDto(
                    weatherId,
                    userId,
                    Collections.emptyList()
            );
            given(recommendationService.recommendation(userId, weatherId)).willReturn(responseDto);

            // when & then
            mockMvc.perform(get("/api/recommendations")
                            .param("userId", userId.toString())
                            .param("weatherId", weatherId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(userId.toString()))
                    .andExpect(jsonPath("$.weatherId").value(weatherId.toString()))
                    .andExpect(jsonPath("$.clothes").isArray());

            then(recommendationService).should(times(1)).recommendation(userId, weatherId);
        }

        @Test
        @DisplayName("[400] userId 파라미터가 없으면 400 Bad Request를 반환")
        void getRecommendation_fail_when_userId_is_missing() throws Exception {
            // given
            UUID weatherId = UUID.randomUUID();

            // when & then
            mockMvc.perform(get("/api/recommendations")
                            .param("weatherId", weatherId.toString()))
                    .andExpect(status().isBadRequest());

            then(recommendationService).should(never()).recommendation(any(), any());
        }

        @Test
        @DisplayName("[400] weatherId 파라미터가 없으면 400 Bad Request를 반환")
        void getRecommendation_fail_when_weatherId_is_missing() throws Exception {
            // given
            UUID userId = UUID.randomUUID();

            // when & then
            mockMvc.perform(get("/api/recommendations")
                            .param("userId", userId.toString()))
                    .andExpect(status().isBadRequest());

            then(recommendationService).should(never()).recommendation(any(), any());
        }
    }
}
