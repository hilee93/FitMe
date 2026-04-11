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

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

        RecommendationTemperatureSummaryDto mockWeather =
                new RecommendationTemperatureSummaryDto(15.0);

        RecommendationProfileSummaryDto mockProfile =
                new RecommendationProfileSummaryDto("MALE", 2);

        List<RecommendationClothesSummaryDto> mockClothes = List.of(
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "셔츠", ClothesType.TOP, "https://example.com/shirt.jpg", List.of()),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "바지", ClothesType.BOTTOM, "https://example.com/pants.jpg", List.of()),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "드레스", ClothesType.DRESS, "https://example.com/dress.jpg", List.of()),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "자켓", ClothesType.OUTER, "https://example.com/jacket.jpg", List.of()),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "모자", ClothesType.HAT, "https://example.com/hat.jpg", List.of())
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

        assertThat(result.clothes()).hasSize(3);

        List<ClothesType> recommendedTypes = result.clothes().stream()
                .map(RecommendationClothesSummaryDto::type)
                .collect(Collectors.toList());

        // 15도(체감 13도)에서 TOP, BOTTOM, OUTER 중에서 추천 (DRESS는 남성 제외)
        assertThat(recommendedTypes).containsAnyOf(ClothesType.BOTTOM, ClothesType.OUTER, ClothesType.TOP);

        assertThat(recommendedTypes).doesNotContain(ClothesType.DRESS);

        result.clothes().forEach(clothes -> {
            assertThat(clothes.clothesId()).isNotNull();
            assertThat(clothes.name()).isNotBlank();
            assertThat(clothes.imageUrl()).isNotBlank();
            assertThat(clothes.attributes()).isNotNull();
        });

        verify(recommendationQueryService).getWeatherById(weatherId);
        verify(recommendationQueryService).getProfileByUserId(userId);
        verify(recommendationQueryService).getClothesByUserId(userId);
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

    @Test
    @DisplayName("최소 2개 보장 - 날씨에 맞는 옷이 부족할 때")
    void recommendation_minimum_two_guaranteed() {
        // given
        UUID userId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();

        // 매우 추운 날씨 (-5도, 체감 -7도)
        RecommendationTemperatureSummaryDto coldWeather =
                new RecommendationTemperatureSummaryDto(-5.0);

        RecommendationProfileSummaryDto mockProfile =
                new RecommendationProfileSummaryDto("FEMALE", 2);

        List<RecommendationClothesSummaryDto> mockClothes = List.of(
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "드레스", ClothesType.DRESS, "https://example.com/dress.jpg", List.of()),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "모자", ClothesType.HAT, "https://example.com/hat.jpg", List.of())
        );

        when(recommendationQueryService.getWeatherById(weatherId)).thenReturn(coldWeather);
        when(recommendationQueryService.getProfileByUserId(userId)).thenReturn(mockProfile);
        when(recommendationQueryService.getClothesByUserId(userId)).thenReturn(mockClothes);

        // when
        RecommendationDto result = recommendationService.recommendation(userId, weatherId);

        // then
        assertThat(result.clothes()).hasSize(2);
        verify(recommendationQueryService).getWeatherById(weatherId);
        verify(recommendationQueryService).getProfileByUserId(userId);
        verify(recommendationQueryService).getClothesByUserId(userId);
    }

    @Test
    @DisplayName("성별 필터링 - 남성에게 드레스 추천 제한")
    void recommendation_male_no_dress() {
        // given
        UUID userId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();

        RecommendationTemperatureSummaryDto warmWeather =
                new RecommendationTemperatureSummaryDto(25.0);

        RecommendationProfileSummaryDto maleProfile =
                new RecommendationProfileSummaryDto("MALE", 2);

        List<RecommendationClothesSummaryDto> mockClothes = List.of(
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "드레스1", ClothesType.DRESS, "https://example.com/dress1.jpg", List.of()),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "드레스2", ClothesType.DRESS, "https://example.com/dress2.jpg", List.of()),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "상의", ClothesType.TOP, "https://example.com/top.jpg", List.of()),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "바지", ClothesType.BOTTOM, "https://example.com/pants.jpg", List.of())
        );

        when(recommendationQueryService.getWeatherById(weatherId)).thenReturn(warmWeather);
        when(recommendationQueryService.getProfileByUserId(userId)).thenReturn(maleProfile);
        when(recommendationQueryService.getClothesByUserId(userId)).thenReturn(mockClothes);

        // when
        RecommendationDto result = recommendationService.recommendation(userId, weatherId);

        // then
        assertThat(result.clothes()).hasSize(2);

        boolean hasDress = result.clothes().stream()
                .anyMatch(clothes -> clothes.type() == ClothesType.DRESS);
        assertThat(hasDress).isFalse();

        verify(recommendationQueryService).getWeatherById(weatherId);
        verify(recommendationQueryService).getProfileByUserId(userId);
        verify(recommendationQueryService).getClothesByUserId(userId);
    }

    @Test
    @DisplayName("성별이 null일 때 모든 옷 추천 가능")
    void recommendation_null_gender_allows_all_clothes() {
        // given
        UUID userId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();

        RecommendationTemperatureSummaryDto warmWeather =
                new RecommendationTemperatureSummaryDto(25.0);

        RecommendationProfileSummaryDto nullGenderProfile =
                new RecommendationProfileSummaryDto(null, 2);

        List<RecommendationClothesSummaryDto> mockClothes = List.of(
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "드레스", ClothesType.DRESS, "https://example.com/dress.jpg", List.of()),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "상의", ClothesType.TOP, "https://example.com/top.jpg", List.of()),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "바지", ClothesType.BOTTOM, "https://example.com/pants.jpg", List.of())
        );

        when(recommendationQueryService.getWeatherById(weatherId)).thenReturn(warmWeather);
        when(recommendationQueryService.getProfileByUserId(userId)).thenReturn(nullGenderProfile);
        when(recommendationQueryService.getClothesByUserId(userId)).thenReturn(mockClothes);

        // when
        RecommendationDto result = recommendationService.recommendation(userId, weatherId);

        // then
        assertThat(result.clothes()).hasSize(3);

        verify(recommendationQueryService).getWeatherById(weatherId);
        verify(recommendationQueryService).getProfileByUserId(userId);
        verify(recommendationQueryService).getClothesByUserId(userId);
    }

    @Test
    @DisplayName("14도에서 다양한 옷 추천 - 최소 보장 로직 동작")
    void recommendation_14_degrees_with_minimum_guarantee() {
        // given
        UUID userId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();

        RecommendationTemperatureSummaryDto weather14 =
                new RecommendationTemperatureSummaryDto(14.0);

        RecommendationProfileSummaryDto maleProfile =
                new RecommendationProfileSummaryDto("MALE", 2);

        List<RecommendationClothesSummaryDto> mockClothes = List.of(
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "상의", ClothesType.TOP, "https://example.com/top.jpg", List.of()),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "모자", ClothesType.HAT, "https://example.com/hat.jpg", List.of()),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "바지", ClothesType.BOTTOM, "https://example.com/pants.jpg", List.of())
        );

        when(recommendationQueryService.getWeatherById(weatherId)).thenReturn(weather14);
        when(recommendationQueryService.getProfileByUserId(userId)).thenReturn(maleProfile);
        when(recommendationQueryService.getClothesByUserId(userId)).thenReturn(mockClothes);

        // when
        RecommendationDto result = recommendationService.recommendation(userId, weatherId);

        // then
        assertThat(result.clothes()).hasSize(2);

        verify(recommendationQueryService).getWeatherById(weatherId);
        verify(recommendationQueryService).getProfileByUserId(userId);
        verify(recommendationQueryService).getClothesByUserId(userId);
    }

    @Test
    @DisplayName("빈 옷장일 때 빈 리스트 반환")
    void recommendation_empty_wardrobe() {
        // given
        UUID userId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();

        RecommendationTemperatureSummaryDto mockWeather =
                new RecommendationTemperatureSummaryDto(15.0);

        RecommendationProfileSummaryDto mockProfile =
                new RecommendationProfileSummaryDto("MALE", 2);

        when(recommendationQueryService.getWeatherById(weatherId)).thenReturn(mockWeather);
        when(recommendationQueryService.getProfileByUserId(userId)).thenReturn(mockProfile);
        when(recommendationQueryService.getClothesByUserId(userId)).thenReturn(Collections.emptyList());

        // when
        RecommendationDto result = recommendationService.recommendation(userId, weatherId);

        // then
        assertThat(result.clothes()).isEmpty();
        verify(recommendationQueryService).getWeatherById(weatherId);
        verify(recommendationQueryService).getProfileByUserId(userId);
        verify(recommendationQueryService).getClothesByUserId(userId);
    }
}
