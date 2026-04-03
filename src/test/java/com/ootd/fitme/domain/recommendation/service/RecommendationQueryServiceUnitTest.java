package com.ootd.fitme.domain.recommendation.service;

import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.recommendation.dto.response.RecommendationClothesSummaryDto;
import com.ootd.fitme.domain.recommendation.dto.response.RecommendationProfileSummaryDto;
import com.ootd.fitme.domain.recommendation.exception.RecommendationProfileDataNotFoundException;
import com.ootd.fitme.domain.recommendation.exception.RecommendationUserNotFoundException;
import com.ootd.fitme.domain.recommendation.repository.RecommendationClothesQueryRepository;
import com.ootd.fitme.domain.recommendation.repository.RecommendationProfileQueryRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.weatherforecast.repository.WeatherForecastRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationQueryService - 유닛 테스트")
class RecommendationQueryServiceUnitTest {

    @InjectMocks
    private RecommendationQueryService recommendationQueryService;

    @Mock
    private RecommendationClothesQueryRepository recommendationClothesQueryRepository;

    @Mock
    private RecommendationProfileQueryRepository recommendationProfileQueryRepository;

    @Mock
    private WeatherForecastRepository weatherForecastRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("유효한 userId로 의상 데이터 조회")
    void getClothesByUserId_success() {
        // given
        UUID userId = UUID.randomUUID();

        List<RecommendationClothesSummaryDto> expectedClothes = List.of(
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "셔츠", ClothesType.TOP, "https://example.com/images/shirt1.jpg"),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "바지", ClothesType.BOTTOM, "https://example.com/images/pants1.jpg"),
                new RecommendationClothesSummaryDto(UUID.randomUUID(), "모자", ClothesType.ACCESSORY, "https://example.com/images/hat1.jpg")
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(User.create("email@test.com", "password")));

        when(recommendationClothesQueryRepository.findClothesByUserId(userId)).thenReturn(expectedClothes);

        // when
        List<RecommendationClothesSummaryDto> result = recommendationQueryService.getClothesByUserId(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).anySatisfy(clothes -> {
            assertThat(clothes.name()).isEqualTo("셔츠");
            assertThat(clothes.type()).isEqualTo(ClothesType.TOP);
        });

        verify(userRepository).findById(userId);
        verify(recommendationClothesQueryRepository).findClothesByUserId(userId);
    }

    @Test
    @DisplayName("유효하지 않은 userId로 의상 데이터 조회 - RecommendationUserNotFoundException 발생")
    void getClothesByUserId_throwsException_whenInvalidUserId() {
        // given
        UUID invalidUserId = UUID.randomUUID();

        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        // when & then
        RecommendationUserNotFoundException exception = assertThrows(
                RecommendationUserNotFoundException.class,
                () -> recommendationQueryService.getClothesByUserId(invalidUserId)
        );

        assertThat(exception.getMessage()).isEqualTo("R-001");
        assertThat(exception.getDetails().get("userId")).isEqualTo(invalidUserId);

        verify(userRepository).findById(invalidUserId);
        verify(recommendationClothesQueryRepository, never()).findClothesByUserId(any());
    }

    @Test
    @DisplayName("유효한 userId로 프로필 조회")
    void getProfileByUserId_success() {
        // given
        UUID userId = UUID.randomUUID();
        RecommendationProfileSummaryDto expectedProfile = new RecommendationProfileSummaryDto("MALE", 75);

        when(recommendationProfileQueryRepository.findProfileByUserId(userId))
                .thenReturn(Optional.of(expectedProfile));

        // when
        RecommendationProfileSummaryDto result = recommendationQueryService.getProfileByUserId(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.gender()).isEqualTo("MALE");
        assertThat(result.temperatureSensitivity()).isEqualTo(75);

        verify(recommendationProfileQueryRepository).findProfileByUserId(userId);
    }

    @Test
    @DisplayName("유효한 userId로 프로필을 못 찾을 경우 - RecommendationProfileDataNotFoundException 발생")
    void getProfileByUserId_throwsException_whenProfileNotFound() {
        // given
        UUID userId = UUID.randomUUID();

        when(recommendationProfileQueryRepository.findProfileByUserId(userId))
                .thenReturn(Optional.empty());

        // when & then
        RecommendationProfileDataNotFoundException exception = assertThrows(
                RecommendationProfileDataNotFoundException.class,
                () -> recommendationQueryService.getProfileByUserId(userId)
        );

        assertThat(exception.getMessage()).isEqualTo("R-003");
        assertThat(exception.getDetails().get("userId")).isEqualTo(userId);

        // Mock 호출 검증
        verify(recommendationProfileQueryRepository).findProfileByUserId(userId);
    }
}
