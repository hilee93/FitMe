package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.clothesattribute.entity.ClothesAttribute;
import com.ootd.fitme.domain.clothesattributeselectablevalue.entity.ClothesAttributeSelectableValue;
import com.ootd.fitme.domain.feed.dto.response.FeedClothesFlatRow;
import com.ootd.fitme.domain.feed.entity.Feed;
import com.ootd.fitme.domain.feedclothes.entity.FeedClothes;
import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;
import com.ootd.fitme.domain.weatherforecast.enums.WindStrengthWord;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import com.ootd.fitme.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({
        JpaAuditingConfig.class,
        QuerydslConfig.class,
        FeedClothesQueryRepositoryImpl.class
})
class FeedClothesQueryRepositoryImplTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private FeedClothesQueryRepository feedClothesQueryRepository;

    @Test
    @DisplayName("feedId로 조회하면 의상과 속성 정보가 flat row 리스트로 반환된다")
    void findFeedClothes_returns_flat_rows_list_with_attribute_info_when_valid_feedId() {
        // given
        User user = em.persist(User.create("email@test.com", "password"));

        Region region = em.persist(
                Region.create(
                        "1234567810",
                        "경기도 남양주시 테스트읍 테스트동",
                        "경기도",
                        "남양주시",
                        "테스트읍 테스트동",
                        "",
                        0.0,
                        0.0,
                        0,
                        0
                )
        );

        WeatherForecast weatherForecast = em.persist(
                WeatherForecast.create(
                        Instant.now(),
                        Instant.now(),
                        SkyStatus.CLEAR,
                        PrecipitationType.NONE,
                        0.0,
                        0.0,
                        9.34,
                        -0.70,
                        5.64,
                        17.55,
                        0.0,
                        10.0,
                        0.0,
                        WindStrengthWord.WEAK,
                        region
                )
        );

        Feed feed = em.persist(
                Feed.create(
                        "테스트 피드 내용",
                        0,
                        0,
                        weatherForecast,
                        user
                )
        );

        Clothes dress = em.persist(
                Clothes.create("원피스", ClothesType.DRESS, user)
        );

        Clothes top = em.persist(
                Clothes.create("상의", ClothesType.TOP, user)
        );

        em.persist(FeedClothes.create(feed, dress));
        em.persist(FeedClothes.create(feed, top));

        Attribute sizeAttribute = em.persist(
                Attribute.create("사이즈")
        );

        SelectableValue free = em.persist(
                SelectableValue.create("FREE", sizeAttribute)
        );

        ClothesAttribute topSize = em.persist(
                ClothesAttribute.create(top, sizeAttribute)
        );

        em.persist(
                ClothesAttributeSelectableValue.create(topSize, free)
        );

        em.flush();
        em.clear();

        // when
        List<FeedClothesFlatRow> result =
                feedClothesQueryRepository.findFeedClothes(feed.getId());

        // then
        assertThat(result).hasSize(2);

        assertThat(result)
                .anySatisfy(row -> { // NOTE: List의 값들중 그 하나의 FeedclothesFlatRow 내부 필드값이 아래처럼 값이 다 똑같은지 테스트하고싶을때 anySatisfy 씀
                    assertThat(row.clothesId()).isEqualTo(dress.getId());
                    assertThat(row.clothesName()).isEqualTo("원피스");
                    assertThat(row.clothesType()).isEqualTo(ClothesType.DRESS);
                    assertThat(row.attributeDefinitionId()).isNull();
                    assertThat(row.attributeDefinitionName()).isNull();
                    assertThat(row.attributeValue()).isNull();
                });

        assertThat(result)
                .anySatisfy(row -> {
                    assertThat(row.clothesId()).isEqualTo(top.getId());
                    assertThat(row.clothesName()).isEqualTo("상의");
                    assertThat(row.clothesType()).isEqualTo(ClothesType.TOP);
                    assertThat(row.attributeDefinitionId()).isEqualTo(sizeAttribute.getId());
                    assertThat(row.attributeDefinitionName()).isEqualTo("사이즈");
                    assertThat(row.attributeValue()).isEqualTo("FREE");
                });
    }
}