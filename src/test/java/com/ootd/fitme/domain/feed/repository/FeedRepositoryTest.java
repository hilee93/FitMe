package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.feed.entity.Feed;
import com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder;
import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.region.repository.RegionRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;
import com.ootd.fitme.domain.weatherforecast.enums.WindStrengthWord;
import com.ootd.fitme.domain.weatherforecast.repository.WeatherForecastRepository;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import com.ootd.fitme.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class, FeedFixtureBuilder.class, JpaAuditingConfig.class})
class FeedRepositoryTest {

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WeatherForecastRepository weatherRepository;

    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private FeedFixtureBuilder feedFixtureBuilder;

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("피드삭제")
    class DeleteFeedTest {
        @Test
        @DisplayName("[Positive] 피드삭제 - 정상적으로 삭제된다 ")
        void deleteFeed_success() {

            // given
            User user = userRepository.save(
                    User.create("email@test.com", "password")
            );

            Region region = regionRepository.save(
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

            WeatherForecast weather = weatherRepository.save(
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

            Feed feed = feedRepository.save(
                    Feed.create("테스트 피드", 0, 0, weather, user)
            );

            UUID feedId = feed.getId();

            // when
            feedRepository.deleteById(feedId);

            // then
            Optional<Feed> result = feedRepository.findById(feedId);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[Negative] 존재하지 않는 Feed 삭제 시 예외 없이 동작한다")
        void deleteFeed_fail_when_feed_not_found() {
            // given
            UUID invalidId = UUID.randomUUID();

            // when
            feedRepository.deleteById(invalidId);

            // then
            assertThat(feedRepository.findById(invalidId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("피드수정")
    class UpdateFeedTest {

        @Test
        @DisplayName("[positive] 피드수정 - content 수정시 DB에 반영된다")
        void updateFeed_success_when_valid_request() {

            //given
            FeedFixtureBuilder.FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            Feed feed = feedFixture.feed();

            // when
            feed.updateContent("수정된 내용");

            em.flush();
            em.clear();

            Feed reloadedFeed = feedRepository.findById(feed.getId()).orElseThrow();
            assertThat(reloadedFeed.getContent()).isEqualTo("수정된 내용");


        }

    }

    @Nested
    @DisplayName("피드 조회")
    class FindFeedTest {

        @Test
        @DisplayName("[Negative] 존재하지 않는 feedId 조회 시 빈 Optional을 반환한다")
        void findFeed_fail_when_feed_not_found() {
            // given
            UUID notExistsFeedId = UUID.randomUUID();

            // when
            Optional<Feed> result = feedRepository.findById(notExistsFeedId);

            // then
            assertThat(result).isEmpty();
        }
    }


}