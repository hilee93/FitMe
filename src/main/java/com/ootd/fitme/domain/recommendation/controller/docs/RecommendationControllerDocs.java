package com.ootd.fitme.domain.recommendation.controller.docs;

import com.ootd.fitme.domain.recommendation.dto.response.RecommendationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.apache.kafka.common.utils.Java;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.UUID;

@Tag(name = "추천 관리", description = "추천 관련 API")
public interface RecommendationControllerDocs {

    @Operation(
            summary = "추천 조회",
            description = "날씨와 인증된 사용자 정보를 기반으로 최적의 옷차림을 추천합니다.",
            parameters = {
                    @Parameter(name = "weatherId", description = "현재 날씨 정보의 ID", required = true)
            }
    )
    ResponseEntity<RecommendationDto> getRecommendation(
            @Parameter(hidden = true) Principal principal,
            @Parameter(description = "현재 날씨 정보의 ID", required = true)
            @NotNull UUID weatherId
    );
}
