package com.ootd.fitme.domain.recommendation.controller.docs;

import com.ootd.fitme.domain.recommendation.dto.response.RecommendationDto;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.apache.kafka.common.utils.Java;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.UUID;

@Tag(name = "추천 관리", description = "추천 관련 API")
public interface RecommendationControllerDocs {

    @Operation(
            summary = "추천 조회",
            description = "사용자와 날씨 ID를 기반으로 추천 데이터를 반환합니다.",
            parameters = {
                    @Parameter(name = "weatherId", description = "날씨 ID", required = true),
                    @Parameter(name = "userPrincipal", hidden = true)
            }
    )
    ResponseEntity<RecommendationDto> getRecommendation(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal userPrincipal,
            @RequestParam("weatherId") @NotNull UUID weatherId
    );
}
