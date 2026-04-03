package com.ootd.fitme.domain.recommendation.controller;

import com.ootd.fitme.domain.recommendation.controller.docs.RecommendationControllerDocs;
import com.ootd.fitme.domain.recommendation.dto.response.RecommendationDto;
import com.ootd.fitme.domain.recommendation.service.RecommendationService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController implements RecommendationControllerDocs {

    private final RecommendationService recommendationService;

    @Override
    @GetMapping
    public ResponseEntity<RecommendationDto> getRecommendation(
            @RequestParam("userId") @NotNull UUID userId,
            @RequestParam("weatherId") @NotNull UUID weatherId) {

        RecommendationDto response = recommendationService.recommendation(userId, weatherId);
        return ResponseEntity.ok(response);
    }
}
