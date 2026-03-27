package com.ootd.fitme.domain.feed.service;

import com.ootd.fitme.domain.comment.dto.response.CommentCursorResponseDto;
import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.feed.dto.request.*;
import com.ootd.fitme.domain.feed.dto.response.FeedCursorResponseDto;
import com.ootd.fitme.domain.feed.dto.response.FeedResponseDto;

import java.util.UUID;

public interface FeedService {
    FeedCursorResponseDto searchFeeds(FeedSearchCondition feedSearchCondition);

    FeedResponseDto createFeed(FeedCreateRequest feedCreateRequest);

    void deleteFeed(UUID feedId);

    FeedResponseDto updateFeed(UUID feedId, UUID userId, FeedUpdateRequestDto feedUpdateRequestDto);

    CommentResponseDto addCommentToFeed(FeedCommentCreateRequest feedCommentCreateRequest);

    CommentCursorResponseDto getFeedComments(FeedCommentSearchCondition feedCommentSearchCondition);

    void likeFeed(UUID feedId, UUID userId);

    void unlikeFeed(UUID feedId, UUID userId);

}
