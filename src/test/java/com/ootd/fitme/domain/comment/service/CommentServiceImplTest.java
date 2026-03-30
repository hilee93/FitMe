package com.ootd.fitme.domain.comment.service;

import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.comment.entity.Comment;
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

}