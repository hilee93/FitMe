package com.ootd.fitme.domain.comment.service;

import com.ootd.fitme.domain.comment.dto.request.CommentSearchCondition;
import com.ootd.fitme.domain.comment.dto.response.CommentCursorResponseDto;
import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.comment.entity.Comment;
import com.ootd.fitme.domain.comment.event.FeedCommentCreateEvent;
import com.ootd.fitme.domain.comment.repository.CommentRepository;
import com.ootd.fitme.domain.feed.dto.request.FeedCommentCreateRequest;
import com.ootd.fitme.domain.feed.entity.Feed;
import com.ootd.fitme.domain.feed.exception.FeedNotFoundException;
import com.ootd.fitme.domain.feed.repository.FeedRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CommentQueryService commentQueryService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CommentResponseDto createFeedComment(FeedCommentCreateRequest feedCommentCreateRequest, UUID userId) {
        Feed feed = feedRepository.findById(feedCommentCreateRequest.feedId()).orElseThrow(() -> new FeedNotFoundException(ErrorCode.FEED_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow();
        Comment comment = Comment.create(feedCommentCreateRequest.content(), feed, user);

        Comment savedComment = commentRepository.save(comment);

        eventPublisher.publishEvent(
                new FeedCommentCreateEvent(
                        savedComment.getId(),
                        feed.getId(),
                        feed.getUser().getId(),
                        feed.getContent(),
                        comment.getUser().getId(),
                        comment.getContent(),
                        savedComment.getCreatedAt()

                )
        );

        feedRepository.increaseCommentCount(feed.getId());

        return commentQueryService.getFeedComment(savedComment.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentCursorResponseDto getFeedComments(CommentSearchCondition feedCommentSearchCondition) {

        return commentQueryService.getComments(feedCommentSearchCondition);
    }
}
