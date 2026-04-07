package com.ootd.fitme.domain.recommendation.controller;

import com.ootd.fitme.domain.recommendation.dto.response.RecommendationDto;
import com.ootd.fitme.domain.recommendation.service.RecommendationService;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import com.ootd.fitme.global.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
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


    // NOTE: MockMvc 테스트에서 인증된 사용자 요청을 만들기 위해 SecurityContext에 Authentication(principal)을 설정한다.
    private RequestPostProcessor userPrincipal(UUID userId) {
        return request -> {
            CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
            given(principal.getUserId()).willReturn(userId);
            given(principal.getAuthorities()).willReturn(List.of());

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            return request;
        };
    }

    @Nested
    @DisplayName("GET /api/recommendations (추천 조회)")
    class GetRecommendationTest {

        @Test
        @DisplayName("[200] 유효한 요청으로 추천 결과와 함께 200 OK 반환")
        void getRecommendation_success_when_validRequest() throws Exception {
            // given

            UUID weatherId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            RecommendationDto responseDto = new RecommendationDto(
                    weatherId,
                    userId,
                    Collections.emptyList()
            );

            given(recommendationService.recommendation(userId, weatherId)).willReturn(responseDto);

            // when & then
            mockMvc.perform(get("/api/recommendations")
                            .param("weatherId", weatherId.toString())
                            .with(userPrincipal(userId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(userId.toString()))
                    .andExpect(jsonPath("$.weatherId").value(weatherId.toString()))
                    .andExpect(jsonPath("$.clothes").isArray());

            then(recommendationService).should(times(1)).recommendation(userId, weatherId);
        }

        @Test
        @DisplayName("[400] weatherId 파라미터가 누락되면 400 반환")
        void getRecommendation_fail_when_weatherId_missing() throws Exception {
            // given
            UUID userId = UUID.randomUUID();

            // when & then
            mockMvc.perform(get("/api/recommendations")
                            .with(userPrincipal(userId)))
                    .andExpect(status().isBadRequest());

            then(recommendationService).should(never()).recommendation(any(), any());
        }
    }
}
