package com.ootd.fitme.domain.feed.service;

import com.ootd.fitme.domain.comment.dto.response.CommentCursorResponseDto;
import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.feed.dto.request.FeedCommentCreateRequest;
import com.ootd.fitme.domain.feed.dto.request.FeedCommentSearchCondition;
import com.ootd.fitme.domain.feed.dto.request.FeedCreateRequest;
import com.ootd.fitme.domain.feed.dto.request.FeedSearchCondition;
import com.ootd.fitme.domain.feed.dto.response.FeedCursorResponseDto;
import com.ootd.fitme.domain.feed.dto.response.FeedResponseDto;

import java.util.UUID;

public interface FeedService {
    FeedCursorResponseDto searchFeeds(FeedSearchCondition feedSearchCondition);

    FeedResponseDto createFeed(FeedCreateRequest feedCreateRequest);

    void deleteFeed(UUID feedId);

    CommentResponseDto addCommentToFeed(FeedCommentCreateRequest feedCommentCreateRequest);

    CommentCursorResponseDto getFeedComments(FeedCommentSearchCondition feedCommentSearchCondition);

    void likeFeed(UUID feedId);

    void unlikeFeed(UUID feedId);

}
