package com.ootd.fitme.domain.recommendation.service;

import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.recommendation.dto.response.RecommendationClothesSummaryDto;
import com.ootd.fitme.domain.recommendation.dto.response.RecommendationProfileSummaryDto;
import com.ootd.fitme.domain.recommendation.dto.response.RecommendationDto;
import com.ootd.fitme.domain.recommendation.dto.response.RecommendationTemperatureSummaryDto;

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

        // 추천 로직에 따른 필터링
        List<RecommendationClothesSummaryDto> recommendedClothes = clothesList.stream()
                .filter(clothes -> isGenderAppropriate(clothes, userProfile.gender()))
                .filter(clothes -> isWeatherSuitable(clothes, feltTemp))
                .limit(5)
                .collect(Collectors.toList());

        return new RecommendationDto(weatherId, userId, recommendedClothes);
    }

    // 성별에 따른 의상 필터링, 남성에게 드레스 추천 제한
    private boolean isGenderAppropriate(RecommendationClothesSummaryDto clothes, String gender) {
        if (gender.equals("MALE") && clothes.type().equals(ClothesType.DRESS)) {
            return false;
        }
        return true;
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

        // 날씨 조건에 따라 필터링
        switch (type) {
            case OUTER:
                return feltTemp < 15; // 외투는 추운 날씨일 때 추천
            case TOP:
                return feltTemp > 15 && feltTemp < 25; // 상의는 온도가 적당한 날 추천
            case DRESS:
                return feltTemp > 20; // 드레스는 더운 날씨에 적합
            case HAT:
                return feltTemp >= 30; // 모자는 30도 이상일 때만 추천
            default:
                return true;
        }
    }

     // 체감 온도 계산
    private double calculateFeltTemperature(double currentTemp, int sensitivity) {
        return currentTemp - (3.0 - sensitivity) * 2;
    }
}
