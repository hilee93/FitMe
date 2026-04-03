package com.ootd.fitme.domain.recommendation.service;

import com.ootd.fitme.domain.recommendation.dto.response.*;
import com.ootd.fitme.domain.recommendation.exception.RecommendationProfileDataNotFoundException;
import com.ootd.fitme.domain.recommendation.exception.RecommendationUserNotFoundException;
import com.ootd.fitme.domain.recommendation.exception.RecommendationWeatherNotFoundException;
import com.ootd.fitme.domain.recommendation.repository.RecommendationClothesQueryRepository;
import com.ootd.fitme.domain.recommendation.repository.RecommendationProfileQueryRepository;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.weatherforecast.repository.WeatherForecastRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationQueryService {

    private final RecommendationClothesQueryRepository recommendationClothesQueryRepository;
    private final RecommendationProfileQueryRepository recommendationProfileQueryRepository;
    private final WeatherForecastRepository weatherForecastRepository;
    private final UserRepository userRepository;

    // 날씨 ID를 기반으로 현재 기상 정보를 조회
    public RecommendationTemperatureSummaryDto getWeatherById(UUID weatherId) {
        return weatherForecastRepository.findById(weatherId)
                .map(weather -> new RecommendationTemperatureSummaryDto(
                        weather.getTemperatureCurrent()
                ))
                .orElseThrow(() -> new RecommendationWeatherNotFoundException(
                        ErrorCode.RECOMMENDATION_WEATHER_NOT_FOUND,
                        Map.of("weatherId", weatherId)
                ));
    }

    // 사용자 ID를 기반으로 사용자 프로필 조회
    public RecommendationProfileSummaryDto getProfileByUserId(UUID userId) {
        return recommendationProfileQueryRepository.findProfileByUserId(userId)
                .orElseThrow(() -> new RecommendationProfileDataNotFoundException(
                        ErrorCode.RECOMMENDATION_PROFILE_DATA_NOT_FOUND,
                        Map.of("userId", userId)
                ));
    }

    // 사용자 ID를 기반으로 옷 데이터 조회
    public List<RecommendationClothesSummaryDto> getClothesByUserId(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RecommendationUserNotFoundException(
                        ErrorCode.RECOMMENDATION_USER_NOT_FOUND,
                        Map.of("userId", userId)
                ));
        return recommendationClothesQueryRepository.findClothesByUserId(userId);
    }
}
