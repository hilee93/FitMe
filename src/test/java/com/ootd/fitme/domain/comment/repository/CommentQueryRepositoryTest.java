package com.ootd.fitme.domain.comment.repository;

import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.comment.entity.Comment;
import com.ootd.fitme.domain.comment.exception.CommentNotFoundException;
import com.ootd.fitme.domain.feed.entity.Feed;
import com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder;
import com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder.FeedFixture;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import com.ootd.fitme.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({JpaAuditingConfig.class,QuerydslConfig.class, CommentQueryRepository.class, FeedFixtureBuilder.class})
class CommentQueryRepositoryTest {
    @Autowired
    private CommentQueryRepository commentQueryRepository;

    @Autowired
    private TestEntityManager em;

    @Autowired
    private FeedFixtureBuilder feedFixtureBuilder;

    @Test
    @DisplayName("댓글이 존재하면 댓글 상세 정보를 조회한다")
    void getFeedComment_success_when_comment_exists() {
        // given
        FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
        User user = feedFixture.user();
        Feed feed = feedFixture.feed();
        Profile profile = feedFixture.profile();

        Comment comment = Comment.create("댓글 내용", feed, user);
        em.persist(comment);

        em.flush();
        em.clear();

        // when
        CommentResponseDto result = commentQueryRepository.getFeedComment(comment.getId());

        // then
        assertThat(result.id()).isEqualTo(comment.getId());
        assertThat(result.feedId()).isEqualTo(feed.getId());
        assertThat(result.author().userId()).isEqualTo(user.getId());
        assertThat(result.author().name()).isEqualTo(profile.getName());
        assertThat(result.author().profileImageUrl()).isEqualTo(profile.getProfileImageUrl());
        assertThat(result.content()).isEqualTo("댓글 내용");
    }

    @Test
    @DisplayName("댓글이 없으면 CommentNotFoundException이 발생한다")
    void getFeedComment_fail_when_comment_not_found() {
        // given
        UUID notExistingCommentId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> commentQueryRepository.getFeedComment(notExistingCommentId))
                .isInstanceOf(CommentNotFoundException.class);
    }
}