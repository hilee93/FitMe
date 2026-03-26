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
    @DisplayName("피드 좋아요 - 좋아요 존재여부")
    class ExistsTest {

        @Test
        @DisplayName("[Positive] 피드 좋아요 - feed에 해당 유저의 좋아요가 존재하면 true 반환")
        void exists_true() {
            // given
            FeedFixtureBuilder.FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            Feed feed = feedFixture.feed();
            User user = feedFixture.user();

            feedLikeRepository.save(FeedLike.create(feed, user));
            em.flush();
            em.clear();

            // when
            boolean result = feedLikeRepository
                    .existsByFeedIdAndUserId(feed.getId(), user.getId());

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("[Negative] 피드 좋아요 - 피드에 해당 유저의 좋아요가 없으면 false 반환")
        void exists_false() {
            // given
            FeedFixtureBuilder.FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            Feed feed = feedFixture.feed();
            User user = feedFixture.user();

            em.flush();
            em.clear();

            // when
            boolean result = feedLikeRepository
                    .existsByFeedIdAndUserId(feed.getId(), user.getId());

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("피드좋아요 - 좋아요 조회")
    class FindTest {

        @Test
        @DisplayName("[Positive] 피드좋아요 - 해당 피드에 유저의 좋아요가 존재하면 Optional 반환")
        void find_success() {
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

        @Test
        @DisplayName("[Negative] 피드좋아요 - 해당 피드에 유저의 좋아요가 없으면 empty 반환")
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
    @DisplayName("피드좋아요 - 좋아요 취소")
    class DeleteTest {

        @Test
        @DisplayName("[Positive] 피드좋아요 - 삭제하면 DB에서 사라진다")
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
            boolean exists = feedLikeRepository
                    .existsByFeedIdAndUserId(feed.getId(), user.getId());

            assertThat(exists).isFalse();
        }
    }
}