package com.ootd.fitme.domain.comment.service;

import com.ootd.fitme.domain.comment.dto.request.CommentSearchCondition;
import com.ootd.fitme.domain.comment.dto.response.CommentCursorResponseDto;
import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.comment.entity.Comment;
import com.ootd.fitme.domain.comment.enums.CommentSortCriteria;
import com.ootd.fitme.domain.comment.enums.SortDirection;
import com.ootd.fitme.domain.comment.exception.CommentNotFoundException;
import com.ootd.fitme.domain.comment.repository.CommentQueryRepository;
import com.ootd.fitme.domain.comment.repository.CommentRepository;
import com.ootd.fitme.domain.feed.dto.request.FeedCommentCreateRequest;
import com.ootd.fitme.domain.feed.entity.Feed;
import com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CommentServiceImplTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private FeedFixtureBuilder feedFixtureBuilder;

    @Autowired
    private CommentService commentService;
    @Autowired
    private CommentQueryRepository commentQueryRepository;
    @Autowired
    private CommentRepository commentRepository;

    @Nested
    @DisplayName("피드 댓글 생성")
    class CreateCommentTest {

        @Test
        @DisplayName("[Positive] 피드댓글 작성 -정상 댓글 생성 성공시 응답 DTO 반환하고 DB에 저장된다.")
        void createComment_success_returnCommentResponseDto() {
            // given
            FeedFixtureBuilder.FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            Feed feed = feedFixture.feed();
            User user = feedFixture.user();


            FeedCommentCreateRequest feedCommentCreateRequest = new FeedCommentCreateRequest(feed.getId(), user.getId(), "댓글 작성");
            // when
            CommentResponseDto feedCommentResponseDto = commentService.createFeedComment(feedCommentCreateRequest, user.getId());

            // then
            assertThat(feedCommentResponseDto).isNotNull();
            assertThat(feedCommentResponseDto.feedId()).isEqualTo(feed.getId());
            assertThat(feedCommentResponseDto.content()).isEqualTo("댓글 작성");

            // then DB 체크
            em.flush();
            em.clear();

            Comment comment = commentRepository.findById(feedCommentResponseDto.id()).orElseThrow(() -> new CommentNotFoundException(ErrorCode.COMMENT_NOT_FOUND));

            assertThat(comment.getContent()).isEqualTo("댓글 작성");
            assertThat(comment.getFeed().getId()).isEqualTo(feed.getId());
            assertThat(comment.getUser().getId()).isEqualTo(user.getId());

        }
    }

    @Nested
    @DisplayName("피드 댓글 목록 조회")
    class GetFeedComments {

        @Test
        @DisplayName("댓글이 30개 있으면 limit 20 조회 시 20개와 hasNext=true를 반환한다")
        void getFeedComments_success_when_has_next() {
            // given
            FeedFixtureBuilder.FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            User user = feedFixture.user();
            Feed feed = feedFixture.feed();

            for (int i = 0; i < 30; i++) {
                Comment comment = Comment.create("댓글 내용" + i, feed, user);
                em.persist(comment);
            }

            em.flush();
            em.clear();

            CommentSearchCondition condition =
                    new CommentSearchCondition(null, null, 20, feed.getId());

            // when
            CommentCursorResponseDto result = commentService.getFeedComments(condition);

            // then
            assertThat(result.data()).hasSize(20);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.totalCount()).isEqualTo(30L);
            assertThat(result.sortBy()).isEqualTo(CommentSortCriteria.CREATED_AT.getValue());
            assertThat(result.sortDirection()).isEqualTo(SortDirection.DESCENDING);
        }

        @Test
        @DisplayName("댓글이 10개 있으면 limit 20 조회 시 10개와 hasNext=false를 반환한다")
        void getFeedComments_success_when_no_next() {
            // given
            FeedFixtureBuilder.FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            User user = feedFixture.user();
            Feed feed = feedFixture.feed();

            for (int i = 0; i < 10; i++) {
                Comment comment = Comment.create("댓글 내용" + i, feed, user);
                em.persist(comment);
            }

            em.flush();
            em.clear();

            CommentSearchCondition condition =
                    new CommentSearchCondition(null, null, 20, feed.getId());

            // when
            CommentCursorResponseDto result = commentService.getFeedComments(condition);

            // then
            assertThat(result.data()).hasSize(10);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.totalCount()).isEqualTo(10L);
        }

        @Test
        @DisplayName("첫 페이지의 nextCursor와 nextIdAfter로 다음 페이지를 조회할 수 있다")
        void getFeedComments_success_next_page() {
            // given
            FeedFixtureBuilder.FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
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

            // when
            CommentCursorResponseDto firstPage = commentService.getFeedComments(firstCondition);

            CommentSearchCondition secondCondition =
                    new CommentSearchCondition(
                            firstPage.nextCursor(),
                            firstPage.nextIdAfter(),
                            20,
                            feed.getId()
                    );

            CommentCursorResponseDto secondPage = commentService.getFeedComments(secondCondition);

            // then
            assertThat(firstPage.data()).hasSize(20);
            assertThat(firstPage.hasNext()).isTrue();

            assertThat(secondPage.data()).hasSize(10);
            assertThat(secondPage.hasNext()).isFalse();
            assertThat(secondPage.totalCount()).isEqualTo(30L);
        }
    }

}