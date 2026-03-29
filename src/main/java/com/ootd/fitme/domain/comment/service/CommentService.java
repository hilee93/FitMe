package com.ootd.fitme.domain.comment.service;

import com.ootd.fitme.domain.comment.dto.response.CommentCursorResponseDto;
import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.feed.dto.request.FeedCommentCreateRequest;
import com.ootd.fitme.domain.feed.dto.request.FeedCommentSearchCondition;

import java.util.UUID;

public interface CommentService {

    CommentResponseDto createFeedComment(FeedCommentCreateRequest feedCommentCreateRequest, UUID userId);

    CommentCursorResponseDto getFeedComments(FeedCommentSearchCondition feedCommentSearchCondition, UUID userId);

}
