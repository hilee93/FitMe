package com.ootd.fitme.domain.comment.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.comment.dto.request.CommentSearchCondition;
import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.comment.entity.Comment;
import com.ootd.fitme.domain.comment.exception.CommentNotFoundException;
import com.ootd.fitme.domain.feed.dto.response.CursorResult;
import com.ootd.fitme.domain.feed.entity.Feed;
import com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder;
import com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder.FeedFixture;
import com.ootd.fitme.domain.profile.entity.Profile;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class, CommentQueryRepository.class, FeedFixtureBuilder.class})
class CommentQueryRepositoryTest {
    @Autowired
    private CommentQueryRepository commentQueryRepository;

    @Autowired
    private TestEntityManager em;

    @Autowired
    private FeedFixtureBuilder feedFixtureBuilder;

    @Test
    @DisplayName("댓글이 존재하면 댓글 상세 정보를 조회한다")
    void findfeedComment_success_when_comment_ById_exists() {
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
        CommentResponseDto result = commentQueryRepository.findFeedCommentById(comment.getId());

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
    void findfeedComment_fail_when_comment_ById_not_found() {
        // given
        UUID notExistingCommentId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> commentQueryRepository.findFeedCommentById(notExistingCommentId))
                .isInstanceOf(CommentNotFoundException.class);
    }

    @Nested
    @DisplayName("댓글 조회")
    class FindFeedComments {

        @Test
        @DisplayName("댓글조회 - 피드 댓글 목록조회시limit 만큼 조회 및 hasNext true")
        void findFeedComments_success_when_valid_request() {
            // given
            FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            User user = feedFixture.user();
            Feed feed = feedFixture.feed();

            for (int i = 0; i < 30; i++) {
                Comment comment = Comment.create("댓글 내용" + i, feed, user);
                em.persist(comment);
            }

            em.flush();
            em.clear();

            CommentSearchCondition condition = new CommentSearchCondition(null, null, 20, feed.getId());

            // when
            CursorResult<CommentResponseDto> commentsByFeedId = commentQueryRepository.findCommentsByFeedId(condition);

            // then
            assertThat(commentsByFeedId.hasNext()).isTrue();
            assertThat(commentsByFeedId.content().size()).isEqualTo(20);
            assertThat(commentsByFeedId.total()).isEqualTo(30);
            assertThat(commentsByFeedId.content().get(0).feedId()).isEqualTo(feed.getId());

        }

        @Test
        @DisplayName("댓글조회 - 댓글 수가 limit보다 적으면 hasNext는 false")
        void findFeedComments_hasNext_false_when_less_than_limit() {
            // given
            FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            User user = feedFixture.user();
            Feed feed = feedFixture.feed();

            for (int i = 0; i < 10; i++) {
                Comment comment = Comment.create("댓글 내용" + i, feed, user);
                em.persist(comment);
            }

            em.flush();
            em.clear();

            CommentSearchCondition condition = new CommentSearchCondition(null, null, 20, feed.getId());

            // when
            CursorResult<CommentResponseDto> result = commentQueryRepository.findCommentsByFeedId(condition);

            // then
            assertThat(result.hasNext()).isFalse();
            assertThat(result.content()).hasSize(10);
            assertThat(result.total()).isEqualTo(10);
        }

        @Test
        @DisplayName("댓글조회 - 첫 페이지의 next cursor로 다음 페이지를 조회하면 남은 댓글이 조회된다")
        void findFeedComments_next_page_success_when_valid_cursor() {
            // given
            FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            User user = feedFixture.user();
            Feed feed = feedFixture.feed();

            for (int i = 0; i < 30; i++) {
                Comment comment = Comment.create("댓글 내용" + i, feed, user);
                em.persist(comment);
            }

            em.flush();
            em.clear();

            CommentSearchCondition firstCondition =
                    new CommentSearchCondition(null, null, 20, feed.getId());

            // when - 첫 페이지 조회
            CursorResult<CommentResponseDto> firstPage =
                    commentQueryRepository.findCommentsByFeedId(firstCondition);

            CommentResponseDto lastOfFirstPage =
                    firstPage.content().get(firstPage.content().size() - 1);

            CommentSearchCondition secondCondition =
                    new CommentSearchCondition(
                            lastOfFirstPage.createdAt().toString(),
                            lastOfFirstPage.id(),
                            20,
                            feed.getId()
                    );

            CursorResult<CommentResponseDto> secondPage =
                    commentQueryRepository.findCommentsByFeedId(secondCondition);

            // then
            assertThat(firstPage.hasNext()).isTrue();
            assertThat(firstPage.content()).hasSize(20);
            assertThat(firstPage.total()).isEqualTo(30);

            assertThat(secondPage.hasNext()).isFalse();
            assertThat(secondPage.content()).hasSize(10);
            assertThat(secondPage.total()).isEqualTo(30);


            Set<UUID> firstIds = firstPage.content().stream().map(CommentResponseDto::id).collect(Collectors.toSet());
            Set<UUID> secondIds = secondPage.content().stream().map(CommentResponseDto::id).collect(Collectors.toSet());

            assertThat(firstIds).doesNotContainAnyElementsOf(secondIds);

        }


        @Test
        @DisplayName("createdAt이 동일한 경우에도 cursor pagination이 정상 동작한다")
        void findFeedComments_success_when_same_createdAt() {
            // given
            FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            User user = feedFixture.user();
            Feed feed = feedFixture.feed();

            Instant sameTime = Instant.parse("2024-01-01T00:00:00Z");

            for (int i = 0; i < 30; i++) {
                Comment comment = Comment.create("댓글 내용" + i, feed, user);

                ReflectionTestUtils.setField(comment, "createdAt", sameTime);

                em.persist(comment);
            }

            em.flush();
            em.clear();

            // 첫 페이지
            CommentSearchCondition firstCondition =
                    new CommentSearchCondition(null, null, 20, feed.getId());

            CursorResult<CommentResponseDto> firstPage =
                    commentQueryRepository.findCommentsByFeedId(firstCondition);

            // then (첫 페이지)
            assertThat(firstPage.hasNext()).isTrue();
            assertThat(firstPage.content()).hasSize(20);
            assertThat(firstPage.total()).isEqualTo(30);

            // 다음 페이지 cursor 추출
            CommentResponseDto last = firstPage.content().get(19);

            CommentSearchCondition secondCondition =
                    new CommentSearchCondition(
                            last.createdAt().toString(),
                            last.id(),
                            20,
                            feed.getId()
                    );

            CursorResult<CommentResponseDto> secondPage =
                    commentQueryRepository.findCommentsByFeedId(secondCondition);

            // then (두 번째 페이지)
            assertThat(secondPage.hasNext()).isFalse();
            assertThat(secondPage.content()).hasSize(10);
            assertThat(secondPage.total()).isEqualTo(30);

            // 핵심 검증: 중복 없음
            assertThat(secondPage.content())
                    .extracting(CommentResponseDto::id)
                    .doesNotContain(
                            firstPage.content().stream()
                                    .map(CommentResponseDto::id)
                                    .toArray(UUID[]::new)
                    );
        }
    }
}