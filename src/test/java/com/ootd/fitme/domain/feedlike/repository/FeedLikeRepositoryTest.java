package com.ootd.fitme.domain.feedlike.repository;

import com.ootd.fitme.domain.feed.entity.Feed;
import com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder;
import com.ootd.fitme.domain.feedlike.entity.FeedLike;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class, FeedFixtureBuilder.class, JpaAuditingConfig.class})
class FeedLikeRepositoryTest {

    @Autowired
    private FeedLikeRepository feedLikeRepository;

    @Autowired
    FeedFixtureBuilder feedFixtureBuilder;

    @Autowired
    private TestEntityManager em;

    @Nested
    @DisplayName("피드 좋아요 생성")
    class CreateFeedLikeTest {

        @Test
        @DisplayName("[Positive] 피드 좋아요 생성 - 저장 후 존재 여부 조회 시 true 반환")
        void create_success_exists_true() {
            // given
            FeedFixtureBuilder.FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            Feed feed = feedFixture.feed();
            User user = feedFixture.user();

            // when
            feedLikeRepository.save(FeedLike.create(feed, user));
            em.flush();
            em.clear();

            // then
            boolean result = feedLikeRepository.existsByFeedIdAndUserId(feed.getId(), user.getId());
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("[Positive] 피드 좋아요 생성 - 저장 후 조회 시 Optional 반환")
        void create_success_find_present() {
            // given
            FeedFixtureBuilder.FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            Feed feed = feedFixture.feed();
            User user = feedFixture.user();

            FeedLike saved = feedLikeRepository.save(FeedLike.create(feed, user));
            em.flush();
            em.clear();

            // when
            Optional<FeedLike> result =
                    feedLikeRepository.findByFeedIdAndUserId(feed.getId(), user.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(saved.getId());
        }
    }

    @Nested
    @DisplayName("피드 좋아요 미존재")
    class NotExistsFeedLikeTest {

        @Test
        @DisplayName("[Negative] 피드 좋아요 - 존재하지 않으면 exists 조회 시 false 반환")
        void exists_false() {
            // given
            FeedFixtureBuilder.FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            Feed feed = feedFixture.feed();
            User user = feedFixture.user();

            em.flush();
            em.clear();

            // when
            boolean result = feedLikeRepository.existsByFeedIdAndUserId(feed.getId(), user.getId());

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("[Negative] 피드 좋아요 - 존재하지 않으면 find 조회 시 empty 반환")
        void find_empty() {
            // given
            FeedFixtureBuilder.FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            Feed feed = feedFixture.feed();
            User user = feedFixture.user();

            em.flush();
            em.clear();

            // when
            Optional<FeedLike> result =
                    feedLikeRepository.findByFeedIdAndUserId(feed.getId(), user.getId());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("피드 좋아요 취소")
    class DeleteFeedLikeTest {

        @Test
        @DisplayName("[Positive] 피드 좋아요 취소 - 삭제 후 존재 여부 조회 시 false 반환")
        void delete_success() {
            // given
            FeedFixtureBuilder.FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            Feed feed = feedFixture.feed();
            User user = feedFixture.user();

            FeedLike feedLike = feedLikeRepository.save(FeedLike.create(feed, user));
            em.flush();
            em.clear();

            // when
            feedLikeRepository.delete(feedLike);
            em.flush();
            em.clear();

            // then
            boolean exists = feedLikeRepository.existsByFeedIdAndUserId(feed.getId(), user.getId());
            assertThat(exists).isFalse();
        }
    }
}