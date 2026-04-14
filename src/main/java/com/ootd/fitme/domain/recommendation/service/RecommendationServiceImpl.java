package com.ootd.fitme.domain.recommendation.service;

import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.recommendation.dto.response.*;
import com.ootd.fitme.infrastructure.ai.AiDataExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationQueryService recommendationQueryService;
    private final AiDataExtractor aiDataExtractor;

    private static final String FALLBACK_REASON =
            "오늘 날씨에 적합한 옷들을 추천했습니다!";

    @Override
    public RecommendationDto recommendation(UUID userId, UUID weatherId) {

        RecommendationTemperatureSummaryDto currentWeather =
                recommendationQueryService.getWeatherById(weatherId);
        RecommendationProfileSummaryDto userProfile =
                recommendationQueryService.getProfileByUserId(userId);
        List<RecommendationClothesSummaryDto> allClothes =
                recommendationQueryService.getClothesByUserId(userId);

        // 먼저 AI 에게 옷 + 이유
        try {
            RecommendationDto aiResult =
                    recommendWithAiFirst(userId, weatherId, currentWeather, userProfile, allClothes);

            if (aiResult != null
                    && aiResult.clothes() != null
                    && !aiResult.clothes().isEmpty()
                    && aiResult.recommendationReason() != null) {
                return aiResult;
            }
            log.warn("AI 결과가 비정상, 알고리즘 fallback 사용");
        } catch (Exception e) {
            log.warn("AI 추천 실패 -> 알고리즘 fallback 사용. cause={}", e.getMessage());
        }

        // 3. 여기까지 왔다 = AI가 실패 or 결과 이상 → 알고리즘 fallback
        return recommendWithAlgorithm(userId, weatherId, currentWeather, userProfile, allClothes);
    }


     // 1순위: AI가 옷 + 이유를 모두 선택하는 경로
    private RecommendationDto recommendWithAiFirst(
            UUID userId,
            UUID weatherId,
            RecommendationTemperatureSummaryDto weather,
            RecommendationProfileSummaryDto profile,
            List<RecommendationClothesSummaryDto> allClothes
    ) {
        // AI에게 줄 원본 데이터 구성
        String weatherInfo = weather.temperature() + "도";
        String userInfo = String.format("%s, 온도민감도 %d단계",
                profile.gender(), profile.temperatureSensitivity());

        String clothesRaw = allClothes.stream()
                .map(c -> String.format("id=%s, name=%s, type=%s",
                        c.clothesId(), c.name(), c.type()))
                .collect(Collectors.joining("\n"));

        String rawData = String.format(
                "날씨 정보: %s\n사용자 프로필: %s\n사용자 옷장 목록:\n%s",
                weatherInfo, userInfo, clothesRaw
        );

        String systemPrompt = """
            당신은 개인 옷장 관리 AI 어시스턴트입니다.

            아래의 [사용자 옷장 목록]에서 오늘 날씨와 사용자 정보를 고려하여
            가장 적절한 상의, 하의, 아우터, 신발 조합을 선택하세요.

            출력 형식(JSON):
            {
              "selectedClothesIds": ["UUID1", "UUID2", ...],
              "reason": "오늘 이 조합이 적합한 이유를 40자 이내 한국어로 설명"
            }

            제약:
            - selectedClothesIds 에는 입력으로 받은 옷의 id만 사용
            - 가능한 한 2~3벌 정도로 조합
            """;

        // AI 호출 → DTO 받기
        RecommendationAiDto aiResult =
                aiDataExtractor.extractData(rawData, systemPrompt, RecommendationAiDto.class);

        if (aiResult == null
                || aiResult.selectedClothesIds() == null
                || aiResult.selectedClothesIds().isEmpty()) {
            return null;
        }

        // AI가 선택한 id들만 필터링
        Set<UUID> selectedIds = new HashSet<>(aiResult.selectedClothesIds());

        List<RecommendationClothesSummaryDto> selectedClothes = allClothes.stream()
                .filter(c -> selectedIds.contains(c.clothesId()))
                .toList();

        if (selectedClothes.isEmpty()) {
            return null;
        }

        String reason = aiResult.reason();
        if (reason == null || reason.isBlank()) {
            reason = FALLBACK_REASON;
        }

        return new RecommendationDto(weatherId, userId, selectedClothes, reason);
    }


    // 2순위: AI가 안 되면 우리 알고리즘으로 옷,이유 결정
    private RecommendationDto recommendWithAlgorithm(
            UUID userId,
            UUID weatherId,
            RecommendationTemperatureSummaryDto weather,
            RecommendationProfileSummaryDto profile,
            List<RecommendationClothesSummaryDto> allClothes
    ) {
        double feltTemp = calculateFeltTemperature(
                weather.temperature(), profile.temperatureSensitivity());

        List<RecommendationClothesSummaryDto> recommendedClothes =
                getMinimumRecommendations(allClothes, profile.gender(), feltTemp);

        String reason = buildAlgorithmReason(weather, profile, recommendedClothes);

        return new RecommendationDto(weatherId, userId, recommendedClothes, reason);
    }

    // 알고리즘이 스스로 만드는 기본 이유
    private String buildAlgorithmReason(RecommendationTemperatureSummaryDto weather,
                                        RecommendationProfileSummaryDto profile,
                                        List<RecommendationClothesSummaryDto> clothes) {
        double temp = weather.temperature();
        boolean hasOuter = clothes.stream().anyMatch(c -> c.type() == ClothesType.OUTER);
        boolean hasTop   = clothes.stream().anyMatch(c -> c.type() == ClothesType.TOP);
        boolean hasBottom= clothes.stream().anyMatch(c -> c.type() == ClothesType.BOTTOM);

        if (temp <= 10 && hasOuter && hasTop && hasBottom) {
            return "추운 날씨라 겉옷과 상·하의를 함께 추천했어요.";
        }
        if (temp >= 25 && hasTop && !hasOuter) {
            return "더운 날씨라 가벼운 상·하의 위주로 추천했어요.";
        }
        return FALLBACK_REASON;
    }

    // 성별에 따른 의상 필터링, 남성에게 드레스 추천 제한
    private boolean isGenderAppropriate(RecommendationClothesSummaryDto clothes, String gender) {
        if (gender == null || gender.isBlank()) {
            return true;
        }

        return !(gender.equalsIgnoreCase("MALE") && clothes.type().equals(ClothesType.DRESS));
    }

    // 날씨와 의상이 적합한지 판단
    private boolean isWeatherSuitable(RecommendationClothesSummaryDto clothes, double feltTemp) {
        ClothesType type = clothes.type();

        if (type == ClothesType.BOTTOM
                || type == ClothesType.SHOES
                || type == ClothesType.UNDERWEAR
                || type == ClothesType.SOCKS
                || type == ClothesType.ACCESSORY) {
            return true;
        }

        // 날씨 조건
        switch (type) {
            case OUTER:
                return feltTemp < 25;
            case TOP:
                return feltTemp > 5 && feltTemp < 35;
            case DRESS:
                return feltTemp > 10;
            case HAT:
                return feltTemp >= 20;
            default:
                return true;
        }
    }

    // 체감 온도 계산
    private double calculateFeltTemperature(double currentTemp, int sensitivity) {
        return currentTemp - (3.0 - sensitivity) * 2;
    }

    // 최소 2개 추천 보장
    private List<RecommendationClothesSummaryDto> getMinimumRecommendations(
            List<RecommendationClothesSummaryDto> allClothes,
            String gender,
            double feltTemp) {

        // 기본 필터링
        List<RecommendationClothesSummaryDto> filtered = allClothes.stream()
                .filter(clothes -> isGenderAppropriate(clothes, gender))
                .filter(clothes -> isWeatherSuitable(clothes, feltTemp))
                .limit(5)
                .collect(Collectors.toList());

        // 2개 이상이면 그대로 반환
        if (filtered.size() >= 2) {
            return filtered;
        }

        // 부족하면 성별만 고려해서 추가
        Set<UUID> alreadySelected = filtered.stream()
                .map(RecommendationClothesSummaryDto::clothesId)
                .collect(Collectors.toSet());

        List<RecommendationClothesSummaryDto> additional = allClothes.stream()
                .filter(clothes -> isGenderAppropriate(clothes, gender))
                .filter(clothes -> !alreadySelected.contains(clothes.clothesId()))
                .limit(2 - filtered.size())
                .collect(Collectors.toList());

        filtered.addAll(additional);

        // 여전히 2개 미만이면 모든 옷에서 추가
        if (filtered.size() < 2) {
            Set<UUID> finalSelected = filtered.stream()
                    .map(RecommendationClothesSummaryDto::clothesId)
                    .collect(Collectors.toSet());

            List<RecommendationClothesSummaryDto> remaining = allClothes.stream()
                    .filter(clothes -> !finalSelected.contains(clothes.clothesId()))
                    .limit(2 - filtered.size())
                    .collect(Collectors.toList());

            filtered.addAll(remaining);
        }

        return filtered;
    }
}