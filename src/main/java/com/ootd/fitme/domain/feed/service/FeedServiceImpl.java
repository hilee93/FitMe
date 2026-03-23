package com.ootd.fitme.domain.feed.service;


import com.ootd.fitme.domain.comment.dto.response.CommentCursorResponseDto;
import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.feed.dto.request.FeedCommentCreateRequest;
import com.ootd.fitme.domain.feed.dto.request.FeedCommentSearchCondition;
import com.ootd.fitme.domain.feed.dto.request.FeedCreateRequest;
import com.ootd.fitme.domain.feed.dto.request.FeedSearchCondition;
import com.ootd.fitme.domain.feed.dto.response.FeedCursorResponseDto;
import com.ootd.fitme.domain.feed.dto.response.FeedResponseDto;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FeedServiceImpl implements FeedService {

    @Override
    public FeedCursorResponseDto searchFeeds(FeedSearchCondition feedSearchCondition) {
        return null; // TODO: 검색로직 작성
    }

    @Override
    public FeedResponseDto createFeed(FeedCreateRequest feedCreateRequest) {
        return null;
    }

    @Override
    public void deleteFeed(UUID feedId) {

    }

    @Override
    public CommentResponseDto addCommentToFeed(FeedCommentCreateRequest feedCommentCreateRequest) {

        return null;
    }

    @Override
    public CommentCursorResponseDto getFeedComments(FeedCommentSearchCondition feedCommentSearchCondition) {

        return null;
    }

    @Override
    public void likeFeed(UUID feedId) {

    }

    @Override
    public void unlikeFeed(UUID feedId) {

    }


}
