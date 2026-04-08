package com.ootd.fitme.domain.recommendation.repository;

import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.enums.Gender;
import com.ootd.fitme.domain.recommendation.dto.response.RecommendationProfileSummaryDto;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import com.ootd.fitme.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class, RecommendationProfileQueryRepositoryImpl.class})
@DisplayName("RecommendationProfileQueryRepositoryImpl 테스트")
class RecommendationProfileQueryRepositoryImplTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private RecommendationProfileQueryRepository recommendationProfileQueryRepository;

    @Nested
    @DisplayName("findProfileByUserId 테스트")
    class FindProfileByUserIdTest {

        @Test
        @DisplayName("userId로 프로필 조회 시 요약 정보를 반환")
        void findProfileByUserId_returnsProfileSummary_whenValidUserId() {
            // given
            User user = em.persist(User.create("test_user@example.com", "password"));

            Profile profile = Profile.create(
                    "test_user_profile",
                    127.0, 38.5, 65, 222,
                    "서울특별시", "강남구", "역삼동",
                    Gender.MALE, LocalDate.of(1990, 1, 1),
                    "https://example.com/profile.png",
                    user
            );
            em.persist(profile);

            em.flush();
            em.clear();

            // when
            Optional<RecommendationProfileSummaryDto> result =
                    recommendationProfileQueryRepository.findProfileByUserId(user.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().temperatureSensitivity()).isEqualTo(profile.getTemperatureSensitivity());
        }

        @Test
        @DisplayName("사용자가 존재하지 않을 경우 빈 Optional 반환")
        void findProfileByUserId_returnsEmpty_whenUserIdDoesNotExist() {
            // given
            UUID invalidUserId = UUID.randomUUID();

            // when
            Optional<RecommendationProfileSummaryDto> result =
                    recommendationProfileQueryRepository.findProfileByUserId(invalidUserId);

            // then
            assertThat(result).isEmpty();
        }
    }
}
