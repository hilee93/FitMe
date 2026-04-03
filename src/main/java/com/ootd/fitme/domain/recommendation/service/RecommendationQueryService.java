package com.ootd.fitme.domain.recommendation.service;

import com.ootd.fitme.domain.recommendation.dto.response.*;
import com.ootd.fitme.domain.recommendation.repository.RecommendationClothesQueryRepository;
import com.ootd.fitme.domain.recommendation.repository.RecommendationProfileQueryRepository;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.weatherforecast.repository.WeatherForecastRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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
                        weather.getTemperatureCurrent()))
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 weatherId입니다."));
    }


    // 사용자 ID를 기반으로 사용자 프로필 조회
    public RecommendationProfileSummaryDto getProfileByUserId(UUID userId) {
        return recommendationProfileQueryRepository.findProfileByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 프로필을 찾을 수 없습니다."));
    }


    // 사용자 ID를 기반으로 옷 데이터 조회
    public List<RecommendationClothesSummaryDto> getClothesByUserId(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 ID입니다."));
        return recommendationClothesQueryRepository.findClothesByUserId(userId);
    }
}
