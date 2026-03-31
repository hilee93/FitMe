package com.ootd.fitme.domain.comment.service;

import com.ootd.fitme.domain.comment.dto.request.CommentSearchCondition;
import com.ootd.fitme.domain.comment.dto.response.CommentCursorResponseDto;
import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.feed.dto.request.FeedCommentCreateRequest;

import java.util.UUID;

public interface CommentService {

    CommentResponseDto createFeedComment(FeedCommentCreateRequest feedCommentCreateRequest, UUID userId);

    CommentCursorResponseDto getFeedComments(CommentSearchCondition feedCommentSearchCondition);

}
