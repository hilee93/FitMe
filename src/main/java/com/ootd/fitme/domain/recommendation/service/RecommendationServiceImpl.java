package com.ootd.fitme.domain.recommendation.service;

import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.recommendation.dto.response.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationQueryService recommendationQueryService;

    @Override
    public RecommendationDto recommendation(UUID userId, UUID weatherId) {
        RecommendationTemperatureSummaryDto currentWeather = recommendationQueryService.getWeatherById(weatherId);
        RecommendationProfileSummaryDto userProfile = recommendationQueryService.getProfileByUserId(userId);
        List<RecommendationClothesSummaryDto> clothesList = recommendationQueryService.getClothesByUserId(userId);

        // 체감 온도 계산
        double feltTemp = calculateFeltTemperature(currentWeather.temperature(), userProfile.temperatureSensitivity());

        // 최소 2개 보장 추천 로직
        List<RecommendationClothesSummaryDto> recommendedClothes = getMinimumRecommendations(
                clothesList, userProfile.gender(), feltTemp);

        return new RecommendationDto(weatherId, userId, recommendedClothes);
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
            List<RecommendationClothesSummaryDto> remaining = allClothes.stream()
                    .filter(clothes -> !alreadySelected.contains(clothes.clothesId()))
                    .limit(2 - filtered.size())
                    .collect(Collectors.toList());

            filtered.addAll(remaining);
        }

        return filtered;
    }
}
