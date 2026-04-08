package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.feed.dto.request.FeedSearchCondition;
import com.ootd.fitme.domain.feed.dto.response.CursorResult;
import com.ootd.fitme.domain.feed.dto.response.FeedBaseFlatRow;
import com.ootd.fitme.domain.feed.dto.response.FeedDetailFlatRow;
import com.ootd.fitme.domain.feed.entity.Feed;
import com.ootd.fitme.domain.feed.enums.FeedSortCriteria;
import com.ootd.fitme.domain.feed.enums.SortDirection;
import com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder;
import com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder.FeedFixture;
import com.ootd.fitme.domain.region.entity.Region;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({
        JpaAuditingConfig.class,
        QuerydslConfig.class,
        FeedQueryRepositoryImpl.class,
        FeedFixtureBuilder.class
})
class FeedQueryRepositoryImplTest {
    @Autowired
    private TestEntityManager em;

    @Autowired
    private FeedQueryRepository feedQueryRepository;

    @Autowired
    private FeedFixtureBuilder feedFixtureBuilder;

    @Test
    @DisplayName("feedId로 조회하면 피드 상세 flat row를 반환한다")
    void findFeedDetail_returns_feed_detail_flat_row_when_valid_feed_id() {
        // given
        User user = em.persist(
                User.create("email@test.com", "password")
        );

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

        WeatherForecast weather = em.persist(
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
                        7,
                        3,
                        weather,
                        user
                )
        );

        em.flush();
        em.clear();

        // when
        Optional<FeedDetailFlatRow> result = feedQueryRepository.findFeedDetail(feed.getId());

        // then
        assertThat(result).isPresent();

        FeedDetailFlatRow row = result.orElseThrow();
        assertThat(row.feedId()).isEqualTo(feed.getId());
        assertThat(row.content()).isEqualTo("테스트 피드 내용");
        assertThat(row.likeCount()).isEqualTo(3);
        assertThat(row.commentCount()).isEqualTo(7);

        assertThat(row.authorId()).isEqualTo(user.getId());

        assertThat(row.weatherId()).isEqualTo(weather.getId());
        assertThat(row.skyStatus()).isEqualTo(SkyStatus.CLEAR);
        assertThat(row.precipitationType()).isEqualTo(PrecipitationType.NONE);
        assertThat(row.precipitationAmount()).isEqualTo(0.0);
        assertThat(row.precipitationProbability()).isEqualTo(0.0);
        assertThat(row.currentTemperature()).isEqualTo(5.64);
        assertThat(row.comparedToDayBefore()).isEqualTo(17.55);
        assertThat(row.temperatureMin()).isEqualTo(0.0);
        assertThat(row.temperatureMax()).isEqualTo(10.0);

        assertThat(row.createdAt()).isNotNull();
        assertThat(row.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 feedId로 조회하면 빈 Optional을 반환한다")
    void findFeedDetail_returns_empty_when_feed_not_found() {
        // given
        UUID notExistsFeedId = UUID.randomUUID();

        // when
        Optional<FeedDetailFlatRow> result = feedQueryRepository.findFeedDetail(notExistsFeedId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("커서 페이징 - 다음 페이지 정상 조회")
    void findFeeds_with_cursor() {
        // given
        FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();


        for (int i = 0; i < 5; i++) {
            feedFixtureBuilder.createFeedFixture();
        }

        em.flush();
        em.clear();

        FeedSearchCondition condition = new FeedSearchCondition(
                null,
                FeedSortCriteria.CREATED_AT,
                SortDirection.DESCENDING,
                null,
                null,
                3,
                null,
                null,
                null
        );

        // when
        CursorResult<FeedBaseFlatRow> firstPage =
                feedQueryRepository.findFeedListFlatRows(condition);

        List<FeedBaseFlatRow> content = firstPage.content();
        FeedBaseFlatRow lastRow = content.get(content.size() - 1);

        FeedSearchCondition nextCondition = new FeedSearchCondition(
                null,
                FeedSortCriteria.CREATED_AT,
                SortDirection.DESCENDING,
                lastRow.createdAt().toString(),
                lastRow.feedId(),
                3,
                null,
                null,
                null
        );

        CursorResult<FeedBaseFlatRow> secondPage =
                feedQueryRepository.findFeedListFlatRows(nextCondition);

        // then
        assertThat(firstPage.content()).hasSize(3);
        assertThat(firstPage.hasNext()).isTrue();

        assertThat(secondPage.content()).isNotEmpty();

        Set<UUID> firstIds = firstPage.content().stream()
                .map(FeedBaseFlatRow::feedId)
                .collect(Collectors.toSet());

        Set<UUID> secondIds = secondPage.content().stream()
                .map(FeedBaseFlatRow::feedId)
                .collect(Collectors.toSet());

        assertThat(firstIds).doesNotContainAnyElementsOf(secondIds);
    }


}