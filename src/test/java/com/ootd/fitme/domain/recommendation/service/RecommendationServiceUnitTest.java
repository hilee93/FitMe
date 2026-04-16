package com.ootd.fitme.domain.recommendation.service;

import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.recommendation.dto.response.*;
import com.ootd.fitme.infrastructure.ai.AiDataExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationServiceImpl - 단위 테스트 (AI 우선, 알고리즘 Fallback)")
class RecommendationServiceUnitTest {

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    @Mock
    private RecommendationQueryService recommendationQueryService;

    @Mock
    private AiDataExtractor aiDataExtractor;

    // 공통 옷장 데이터 유틸
    private List<RecommendationClothesSummaryDto> createDefaultClothes() {
        return List.of(
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "셔츠", ClothesType.TOP, "https://example.com/shirt.jpg", List.of()),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "바지", ClothesType.BOTTOM, "https://example.com/pants.jpg", List.of()),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "드레스", ClothesType.DRESS, "https://example.com/dress.jpg", List.of()),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "자켓", ClothesType.OUTER, "https://example.com/jacket.jpg", List.of()),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "모자", ClothesType.HAT, "https://example.com/hat.jpg", List.of())
        );
    }

    // 1. AI가 옷 + 이유를 정상적으로 추천하는 경우
    @Test
    @DisplayName("AI가 옷 + 이유를 정상적으로 추천하면 그 결과를 사용한다")
    void recommendation_success_ai_first() {
        // given
        UUID userId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();

        RecommendationTemperatureSummaryDto mockWeather =
                new RecommendationTemperatureSummaryDto(15.0);
        RecommendationProfileSummaryDto mockProfile =
                new RecommendationProfileSummaryDto("MALE", 2);

        List<RecommendationClothesSummaryDto> mockClothes = createDefaultClothes();

        // AI가 선택할 옷 3개 (TOP, BOTTOM, OUTER) 의 id
        List<UUID> aiSelectedIds = mockClothes.stream()
                .filter(c -> Set.of(ClothesType.TOP, ClothesType.BOTTOM, ClothesType.OUTER).contains(c.type()))
                .map(RecommendationClothesSummaryDto::clothesId)
                .toList();

        String aiReason = "AI가 생성한 설명입니다.";

        when(recommendationQueryService.getWeatherById(weatherId)).thenReturn(mockWeather);
        when(recommendationQueryService.getProfileByUserId(userId)).thenReturn(mockProfile);
        when(recommendationQueryService.getClothesByUserId(userId)).thenReturn(mockClothes);

        // AI가 옷+이유를 한 번에 반환
        when(aiDataExtractor.extractData(anyString(), anyString(), eq(RecommendationAiDto.class)))
                .thenReturn(new RecommendationAiDto(aiSelectedIds, aiReason));

        // when
        RecommendationDto result = recommendationService.recommendation(userId, weatherId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.weatherId()).isEqualTo(weatherId);
        assertThat(result.userId()).isEqualTo(userId);

        // AI가 선택한 3개만 있어야 함
        assertThat(result.clothes()).hasSize(3);
        List<UUID> resultIds = result.clothes().stream()
                .map(RecommendationClothesSummaryDto::clothesId)
                .toList();
        assertThat(resultIds).containsExactlyInAnyOrderElementsOf(aiSelectedIds);

        // 이유도 AI가 준 그대로
        assertThat(result.recommendationReason()).isEqualTo(aiReason);

        List<ClothesType> types = result.clothes().stream()
                .map(RecommendationClothesSummaryDto::type)
                .collect(Collectors.toList());
        assertThat(types)
                .containsExactlyInAnyOrder(ClothesType.TOP, ClothesType.BOTTOM, ClothesType.OUTER);

        verify(recommendationQueryService).getWeatherById(weatherId);
        verify(recommendationQueryService).getProfileByUserId(userId);
        verify(recommendationQueryService).getClothesByUserId(userId);
        verify(aiDataExtractor).extractData(anyString(), anyString(), eq(RecommendationAiDto.class));
    }

    // 2. AI 결과가 비정상 → 알고리즘 fallback

    @Test
    @DisplayName("AI가 null을 반환하면 알고리즘 fallback 이 사용된다")
    void recommendation_ai_returns_null_fallback_algorithm() {
        // given
        UUID userId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();

        RecommendationTemperatureSummaryDto mockWeather =
                new RecommendationTemperatureSummaryDto(15.0);
        RecommendationProfileSummaryDto mockProfile =
                new RecommendationProfileSummaryDto("MALE", 2);
        List<RecommendationClothesSummaryDto> mockClothes = createDefaultClothes();

        when(recommendationQueryService.getWeatherById(weatherId)).thenReturn(mockWeather);
        when(recommendationQueryService.getProfileByUserId(userId)).thenReturn(mockProfile);
        when(recommendationQueryService.getClothesByUserId(userId)).thenReturn(mockClothes);

        when(aiDataExtractor.extractData(anyString(), anyString(), eq(RecommendationAiDto.class)))
                .thenReturn(null); // 비정상 응답

        // when
        RecommendationDto result = recommendationService.recommendation(userId, weatherId);

        // then: 알고리즘 로직이 동작
        assertThat(result).isNotNull();
        assertThat(result.clothes().size()).isBetween(2, 5);

        List<ClothesType> types = result.clothes().stream()
                .map(RecommendationClothesSummaryDto::type)
                .toList();
        // 남성에게 DRESS는 제외
        assertThat(types).doesNotContain(ClothesType.DRESS);

        assertThat(result.recommendationReason()).isNotBlank();

        verify(aiDataExtractor).extractData(anyString(), anyString(), eq(RecommendationAiDto.class));
    }

    @Test
    @DisplayName("AI가 예외를 던지면 알고리즘 fallback 이 사용된다")
    void recommendation_ai_throws_exception_fallback_algorithm() {
        // given
        UUID userId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();

        RecommendationTemperatureSummaryDto mockWeather =
                new RecommendationTemperatureSummaryDto(5.0);
        RecommendationProfileSummaryDto mockProfile =
                new RecommendationProfileSummaryDto("FEMALE", 2);
        List<RecommendationClothesSummaryDto> mockClothes = createDefaultClothes();

        when(recommendationQueryService.getWeatherById(weatherId)).thenReturn(mockWeather);
        when(recommendationQueryService.getProfileByUserId(userId)).thenReturn(mockProfile);
        when(recommendationQueryService.getClothesByUserId(userId)).thenReturn(mockClothes);

        when(aiDataExtractor.extractData(anyString(), anyString(), eq(RecommendationAiDto.class)))
                .thenThrow(new RuntimeException("AI 서버 오류"));

        // when
        RecommendationDto result = recommendationService.recommendation(userId, weatherId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.clothes().size()).isBetween(2, 5);
        assertThat(result.recommendationReason()).isNotBlank();

        verify(aiDataExtractor).extractData(anyString(), anyString(), eq(RecommendationAiDto.class));
    }

    // 3. 기존 예외/필터링/최소 보장 로직 (AI가 실패했다고 가정하고 검증)

    @Test
    @DisplayName("유효하지 않은 userId로 추천 시 - 예외 발생 (AI 호출 안 함)")
    void recommendation_throwsException_whenInvalidUserId() {
        UUID invalidUserId = UUID.randomUUID();
        UUID validWeatherId = UUID.randomUUID();

        when(recommendationQueryService.getProfileByUserId(invalidUserId))
                .thenThrow(new IllegalArgumentException("사용자 프로필을 찾을 수 없습니다."));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> recommendationService.recommendation(invalidUserId, validWeatherId));

        assertThat(ex.getMessage()).isEqualTo("사용자 프로필을 찾을 수 없습니다.");
        verify(recommendationQueryService).getProfileByUserId(invalidUserId);
        verifyNoInteractions(aiDataExtractor);
    }

    @Test
    @DisplayName("유효하지 않은 weatherId로 추천 시 - 예외 발생 (AI 호출 안 함)")
    void recommendation_throwsException_whenInvalidWeatherId() {
        UUID validUserId = UUID.randomUUID();
        UUID invalidWeatherId = UUID.randomUUID();

        when(recommendationQueryService.getWeatherById(invalidWeatherId))
                .thenThrow(new IllegalArgumentException("유효하지 않은 weatherId입니다."));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> recommendationService.recommendation(validUserId, invalidWeatherId));

        assertThat(ex.getMessage()).isEqualTo("유효하지 않은 weatherId입니다.");
        verify(recommendationQueryService).getWeatherById(invalidWeatherId);
        verifyNoInteractions(aiDataExtractor);
    }

    @Test
    @DisplayName("성별 필터링 - 남성에게 드레스 추천 제한 (AI 실패 상황 가정)")
    void recommendation_male_no_dress_algorithm() {
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

        // AI 실패 가정 (null)
        when(aiDataExtractor.extractData(anyString(), anyString(), eq(RecommendationAiDto.class)))
                .thenReturn(null);

        RecommendationDto result = recommendationService.recommendation(userId, weatherId);

        assertThat(result.clothes()).hasSize(2);
        boolean hasDress = result.clothes().stream()
                .anyMatch(c -> c.type() == ClothesType.DRESS);
        assertThat(hasDress).isFalse();
    }

    @Test
    @DisplayName("성별이 null일 때 모든 옷 추천 가능 (AI 실패 상황 가정)")
    void recommendation_null_gender_allows_all_clothes_algorithm() {
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

        when(aiDataExtractor.extractData(anyString(), anyString(), eq(RecommendationAiDto.class)))
                .thenReturn(null);

        RecommendationDto result = recommendationService.recommendation(userId, weatherId);

        assertThat(result.clothes()).hasSize(3);
    }

    @Test
    @DisplayName("빈 옷장일 때 빈 리스트 반환 (AI 실패 상황 가정)")
    void recommendation_empty_wardrobe_algorithm() {
        UUID userId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();

        RecommendationTemperatureSummaryDto mockWeather =
                new RecommendationTemperatureSummaryDto(15.0);
        RecommendationProfileSummaryDto mockProfile =
                new RecommendationProfileSummaryDto("MALE", 2);

        when(recommendationQueryService.getWeatherById(weatherId)).thenReturn(mockWeather);
        when(recommendationQueryService.getProfileByUserId(userId)).thenReturn(mockProfile);
        when(recommendationQueryService.getClothesByUserId(userId)).thenReturn(Collections.emptyList());

        when(aiDataExtractor.extractData(anyString(), anyString(), eq(RecommendationAiDto.class)))
                .thenReturn(null);

        RecommendationDto result = recommendationService.recommendation(userId, weatherId);

        assertThat(result.clothes()).isEmpty();
    }

    @Test
    @DisplayName("모자만 여러 개 있어도 추천 결과에는 모자가 최대 1개만 포함된다")
    void recommendation_algorithm_limits_hat_to_one() {
        // given
        UUID userId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();

        // 모자 추천이 가능한 온도
        RecommendationTemperatureSummaryDto warmWeather =
                new RecommendationTemperatureSummaryDto(25.0);
        RecommendationProfileSummaryDto profile =
                new RecommendationProfileSummaryDto("FEMALE", 2);

        // 옷장에 모자만 여러 개 있는 상황
        List<RecommendationClothesSummaryDto> hatOnlyClothes = List.of(
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "모자1", ClothesType.HAT, "https://example.com/hat1.jpg", List.of()),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "모자2", ClothesType.HAT, "https://example.com/hat2.jpg", List.of()),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "모자3", ClothesType.HAT, "https://example.com/hat3.jpg", List.of())
        );

        when(recommendationQueryService.getWeatherById(weatherId)).thenReturn(warmWeather);
        when(recommendationQueryService.getProfileByUserId(userId)).thenReturn(profile);
        when(recommendationQueryService.getClothesByUserId(userId)).thenReturn(hatOnlyClothes);

        // AI 실패 가정 → 알고리즘 경로로 진입
        when(aiDataExtractor.extractData(anyString(), anyString(), eq(RecommendationAiDto.class)))
                .thenReturn(null);

        // when
        RecommendationDto result = recommendationService.recommendation(userId, weatherId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.clothes()).isNotEmpty();

        long hatCount = result.clothes().stream()
                .filter(c -> c.type() == ClothesType.HAT)
                .count();

        // 모자는 최대 1개만 포함되어야 함
        assertThat(hatCount).isLessThanOrEqualTo(1);
    }

    @Test
    @DisplayName("필터 결과가 1개뿐이어도 최소 2개는 추천된다")
    void recommendation_algorithm_guarantees_minimum_two_items() {
        // given
        UUID userId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();

        // 적당한 온도 값
        RecommendationTemperatureSummaryDto weather =
                new RecommendationTemperatureSummaryDto(20.0);
        RecommendationProfileSummaryDto profile =
                new RecommendationProfileSummaryDto("FEMALE", 2);

        // 예시:
        // - TOP 은 isWeatherSuitable 에 의해 true
        // - 다른 한 벌은 어떤 이유로든 필터에서 탈락하더라도
        //   -> getMinimumRecommendations 가 allClothes 에서 추가로 채워 넣어 최소 2개 보장
        RecommendationClothesSummaryDto top = new RecommendationClothesSummaryDto(
                UUID.randomUUID(), "상의1", ClothesType.TOP, "https://example.com/top1.jpg", List.of());
        RecommendationClothesSummaryDto bottom = new RecommendationClothesSummaryDto(
                UUID.randomUUID(), "하의1", ClothesType.BOTTOM, "https://example.com/bottom1.jpg", List.of());

        List<RecommendationClothesSummaryDto> clothes = List.of(top, bottom);

        when(recommendationQueryService.getWeatherById(weatherId)).thenReturn(weather);
        when(recommendationQueryService.getProfileByUserId(userId)).thenReturn(profile);
        when(recommendationQueryService.getClothesByUserId(userId)).thenReturn(clothes);

        // AI 실패 가정 -> 알고리즘 경로
        when(aiDataExtractor.extractData(anyString(), anyString(), eq(RecommendationAiDto.class)))
                .thenReturn(null);

        // when
        RecommendationDto result = recommendationService.recommendation(userId, weatherId);

        // then
        assertThat(result).isNotNull();
        // 최소 2개 이상 추천되는지 확인
        assertThat(result.clothes().size()).isGreaterThanOrEqualTo(2);
    }
}