package com.ootd.fitme.domain.feed.repository;


import com.ootd.fitme.domain.feed.entity.Feed;
import com.ootd.fitme.domain.feedlike.entity.FeedLike;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
        JpaAuditingConfig.class,
        QuerydslConfig.class,
        FeedLikeQueryRepositoryImpl.class
})
class FeedLikeQueryRepositoryImplTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private FeedLikeQueryRepository feedLikeQueryRepository;

    @Test
    @DisplayName("좋아요가 존재하면 true를 반환한다")
    void existsLike_returns_true_when_like_exists() {
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
                        0,
                        0,
                        weather,
                        user
                )
        );

        em.persist(
                FeedLike.create(feed, user)
        );

        em.flush();
        em.clear();

        // when
        boolean result = feedLikeQueryRepository.existsLike(feed.getId(), user.getId());

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("좋아요가 존재하지 않으면 false를 반환한다")
    void existsLike_returns_false_when_like_not_exists() {
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
                        0,
                        0,
                        weather,
                        user
                )
        );

        em.flush();
        em.clear();

        // when
        boolean result = feedLikeQueryRepository.existsLike(feed.getId(), user.getId());

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("다른 사용자의 좋아요는 false를 반환한다")
    void existsLike_returns_false_when_like_exists_for_other_user() {
        // given
        User author = em.persist(
                User.create("author@test.com", "password")
        );

        User liker = em.persist(
                User.create("liker@test.com", "password")
        );

        User otherUser = em.persist(
                User.create("other@test.com", "password")
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
                        0,
                        0,
                        weather,
                        author
                )
        );

        em.persist(
                FeedLike.create(feed, liker)
        );

        em.flush();
        em.clear();

        // when
        boolean result = feedLikeQueryRepository.existsLike(feed.getId(), otherUser.getId());

        // then
        assertThat(result).isFalse();
    }
}