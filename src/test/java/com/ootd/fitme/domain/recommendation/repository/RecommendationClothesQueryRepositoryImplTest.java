package com.ootd.fitme.domain.recommendation.repository;

import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.recommendation.dto.response.RecommendationClothesSummaryDto;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class, RecommendationClothesQueryRepositoryImpl.class})
@DisplayName("RecommendationClothesQueryRepositoryImpl 테스트")
class RecommendationClothesQueryRepositoryImplTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private RecommendationClothesQueryRepository recommendationClothesQueryRepository;

    @Nested
    @DisplayName("findClothesByUserId 테스트")
    class FindClothesByUserIdTest {

        @Test
        @DisplayName("userId로 조회 시 의상 목록을 반환")
        void findClothesByUserId_returnsClothesSummaryDtoList_whenValidUserId() {
            // given
            User user = em.persist(User.create("test_user@example.com", "password"));

            Clothes clothes1 = em.persist(Clothes.createWithImage(
                    "셔츠",
                    ClothesType.TOP,
                    user,
                    "https://example.com/shirt.jpg" // imageUrl 포함
            ));

            Clothes clothes2 = em.persist(Clothes.createWithImage(
                    "바지",
                    ClothesType.BOTTOM,
                    user,
                    "https://example.com/pants.jpg"
            ));

            Clothes clothes3 = em.persist(Clothes.createWithImage(
                    "자켓",
                    ClothesType.OUTER,
                    user,
                    "https://example.com/jacket.jpg"
            ));

            em.flush();
            em.clear();

            // when
            List<RecommendationClothesSummaryDto> result =
                    recommendationClothesQueryRepository.findClothesByUserId(user.getId());

            // then
            assertThat(result).hasSize(3);

            // 검증
            assertThat(result)
                    .anySatisfy(row -> {
                        assertThat(row.name()).isEqualTo("셔츠");
                        assertThat(row.type()).isEqualTo(ClothesType.TOP);
                        assertThat(row.imageUrl()).isEqualTo("https://example.com/shirt.jpg");
                    });

            assertThat(result)
                    .anySatisfy(row -> {
                        assertThat(row.name()).isEqualTo("바지");
                        assertThat(row.type()).isEqualTo(ClothesType.BOTTOM);
                        assertThat(row.imageUrl()).isEqualTo("https://example.com/pants.jpg");
                    });

            assertThat(result)
                    .anySatisfy(row -> {
                        assertThat(row.name()).isEqualTo("자켓");
                        assertThat(row.type()).isEqualTo(ClothesType.OUTER);
                        assertThat(row.imageUrl()).isEqualTo("https://example.com/jacket.jpg");
                    });
        }

        @Test
        @DisplayName("사용자가 존재하지 않을 경우 빈 리스트 반환")
        void findClothesByUserId_returnsEmptyList_whenInvalidUserId() {
            // given
            UUID invalidUserId = UUID.randomUUID();

            // when
            List<RecommendationClothesSummaryDto> result =
                    recommendationClothesQueryRepository.findClothesByUserId(invalidUserId);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }
    }
}
