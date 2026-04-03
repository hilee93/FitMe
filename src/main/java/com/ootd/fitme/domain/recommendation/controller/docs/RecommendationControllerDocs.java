package com.ootd.fitme.domain.recommendation.controller.docs;

import com.ootd.fitme.domain.recommendation.dto.response.RecommendationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "추천 관리", description = "추천 관련 API")
public interface RecommendationControllerDocs {

    @Operation(summary = "추천 조회", description = "날씨와 사용자 정보를 기반으로 최적의 옷차림을 추천합니다.")
    ResponseEntity<RecommendationDto> getRecommendation(
            @Parameter(description = "추천을 받을 사용자의 ID", required = true)
            @NotNull UUID userId,

            @Parameter(description = "현재 날씨 정보의 ID", required = true)
            @NotNull UUID weatherId
    );
}
