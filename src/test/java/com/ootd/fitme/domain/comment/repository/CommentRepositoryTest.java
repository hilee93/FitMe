package com.ootd.fitme.domain.comment.repository;

import com.ootd.fitme.domain.comment.entity.Comment;
import com.ootd.fitme.domain.feed.entity.Feed;
import com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder;
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

import static com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class, FeedFixtureBuilder.class})
class CommentRepositoryTest {
    @Autowired
    CommentRepository commentRepository;

    @Autowired
    FeedFixtureBuilder feedFixtureBuilder;

    @Autowired
    TestEntityManager em;

    @Nested
    @DisplayName("피드 댓글 생성")
    class SaveAndFindByIdTest {

        @Test
        @DisplayName("[Positive] 피드 댓글 생성 - 댓글을 저장한 후 ID로 조회할 수 있다")
        void createFeedComment_save_and_findById() {
            // given
            FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            Feed feed = feedFixture.feed();
            User user = feedFixture.user();

            Comment comment = Comment.create("댓글", feed, user);

            // FeedFixtureBuilder가 persist 안 해주는 구조라면 유지
            em.persist(user);
            em.persist(feed);

            // when
            Comment saved = commentRepository.save(comment);
            em.flush();
            em.clear();

            // then
            Comment found = commentRepository.findById(saved.getId()).orElseThrow();

            assertThat(found.getId()).isEqualTo(saved.getId());
            assertThat(found.getContent()).isEqualTo("댓글");
            assertThat(found.getFeed().getId()).isEqualTo(feed.getId());
            assertThat(found.getUser().getId()).isEqualTo(user.getId());
        }
    }
}