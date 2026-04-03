package com.ootd.fitme.domain.recommendation.service;

import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.recommendation.dto.response.RecommendationClothesSummaryDto;
import com.ootd.fitme.domain.recommendation.dto.response.RecommendationDto;
import com.ootd.fitme.domain.recommendation.dto.response.RecommendationProfileSummaryDto;
import com.ootd.fitme.domain.recommendation.dto.response.RecommendationTemperatureSummaryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationServiceImpl - 통합 테스트")
class RecommendationServiceUnitTest {

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    @Mock
    private RecommendationQueryService recommendationQueryService;

    @Test
    @DisplayName("유효한 userId와 weatherId로 추천 성공")
    void recommendation_success() {
        // given
        UUID userId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();

        // 현재 온도 15도
        RecommendationTemperatureSummaryDto mockWeather =
                new RecommendationTemperatureSummaryDto(15.0);

        // 남성, 온도 민감도
        RecommendationProfileSummaryDto mockProfile =
                new RecommendationProfileSummaryDto("MALE", 2);

        List<RecommendationClothesSummaryDto> mockClothes = List.of(
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "셔츠", ClothesType.TOP, "https://example.com/shirt.jpg"),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "바지", ClothesType.BOTTOM, "https://example.com/pants.jpg"),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "드레스", ClothesType.DRESS, "https://example.com/dress.jpg"),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "자켓", ClothesType.OUTER, "https://example.com/jacket.jpg"),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "모자", ClothesType.HAT, "https://example.com/hat.jpg")
        );

        when(recommendationQueryService.getWeatherById(weatherId)).thenReturn(mockWeather);
        when(recommendationQueryService.getProfileByUserId(userId)).thenReturn(mockProfile);
        when(recommendationQueryService.getClothesByUserId(userId)).thenReturn(mockClothes);

        // when
        RecommendationDto result = recommendationService.recommendation(userId, weatherId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.weatherId()).isEqualTo(weatherId);
        assertThat(result.userId()).isEqualTo(userId);

        // 추천된 옷 리스트 제한
        assertThat(result.clothes()).isNotNull();
        assertThat(result.clothes()).hasSizeLessThanOrEqualTo(5);

        // 남성(MALE)에게 드레스 추천 금지
        result.clothes().forEach(clothes -> {
            assertThat(clothes.type()).isNotEqualTo(ClothesType.DRESS);
        });
    }

    @Test
    @DisplayName("유효하지 않은 userId로 추천 시 - 예외 발생")
    void recommendation_throwsException_whenInvalidUserId() {
        // given
        UUID invalidUserId = UUID.randomUUID();
        UUID validWeatherId = UUID.randomUUID();

        when(recommendationQueryService.getProfileByUserId(invalidUserId))
                .thenThrow(new IllegalArgumentException("사용자 프로필을 찾을 수 없습니다."));

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> recommendationService.recommendation(invalidUserId, validWeatherId));

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("사용자 프로필을 찾을 수 없습니다.");
        verify(recommendationQueryService).getProfileByUserId(invalidUserId);
    }

    @Test
    @DisplayName("유효하지 않은 weatherId로 추천 시 - 예외 발생")
    void recommendation_throwsException_whenInvalidWeatherId() {
        // given
        UUID validUserId = UUID.randomUUID();
        UUID invalidWeatherId = UUID.randomUUID();

        when(recommendationQueryService.getWeatherById(invalidWeatherId))
                .thenThrow(new IllegalArgumentException("유효하지 않은 weatherId입니다."));

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> recommendationService.recommendation(validUserId, invalidWeatherId));

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("유효하지 않은 weatherId입니다.");
        verify(recommendationQueryService).getWeatherById(invalidWeatherId);
    }
}
