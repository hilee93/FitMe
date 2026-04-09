package com.ootd.fitme.domain.recommendation.repository;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.clothesattribute.entity.ClothesAttribute;
import com.ootd.fitme.domain.clothesattributeselectablevalue.entity.ClothesAttributeSelectableValue;
import com.ootd.fitme.domain.recommendation.dto.response.RecommendationClothesSummaryDto;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
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
        @DisplayName("userId로 조회 시 의상 목록과 속성 포함 반환")
        void findClothesByUserId_returnsClothesAndAttributes() {
            // Given
            User user = em.persist(User.create("test_user@example.com", "password"));

            Clothes clothes1 = em.persist(Clothes.createWithImage(
                    "셔츠",
                    ClothesType.TOP,
                    user,
                    "https://example.com/shirt.jpg"
            ));
            Clothes clothes2 = em.persist(Clothes.createWithImage(
                    "바지",
                    ClothesType.BOTTOM,
                    user,
                    "https://example.com/pants.jpg"
            ));

            Attribute colorAttribute = em.persist(Attribute.create("Color"));
            ClothesAttribute clothesAttribute1 = em.persist(ClothesAttribute.create(clothes1, colorAttribute));

            SelectableValue blueValue = em.persist(SelectableValue.create("Blue", 1, colorAttribute));
            em.persist(ClothesAttributeSelectableValue.create(clothesAttribute1, blueValue));

            em.flush();
            em.clear();

            // When
            List<RecommendationClothesSummaryDto> result =
                    recommendationClothesQueryRepository.findClothesByUserId(user.getId());

            // Then
            assertThat(result).hasSize(2);

            assertThat(result)
                    .anySatisfy(row -> {
                        assertThat(row.name()).isEqualTo("셔츠");
                        assertThat(row.type()).isEqualTo(ClothesType.TOP);
                        assertThat(row.imageUrl()).isEqualTo("https://example.com/shirt.jpg");
                        assertThat(row.attributes()).hasSize(1);
                        assertThat(row.attributes().get(0).definitionName()).isEqualTo("Color");
                        assertThat(row.attributes().get(0).selectableValues()).containsExactly("Blue");
                        assertThat(row.attributes().get(0).value()).isEqualTo("Blue");
                    });

            assertThat(result)
                    .anySatisfy(row -> {
                        assertThat(row.name()).isEqualTo("바지");
                        assertThat(row.type()).isEqualTo(ClothesType.BOTTOM);
                        assertThat(row.imageUrl()).isEqualTo("https://example.com/pants.jpg");
                        assertThat(row.attributes()).isEmpty();
                    });
        }
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
