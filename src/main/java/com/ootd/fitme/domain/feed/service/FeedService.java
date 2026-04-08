package com.ootd.fitme.domain.feed.service;

import com.ootd.fitme.domain.feed.dto.request.*;
import com.ootd.fitme.domain.feed.dto.response.FeedCursorResponseDto;
import com.ootd.fitme.domain.feed.dto.response.FeedResponseDto;

import java.util.UUID;

public interface FeedService {
    FeedCursorResponseDto searchFeeds(FeedSearchCondition feedSearchCondition, UUID userId);

    FeedResponseDto createFeed(FeedCreateRequest feedCreateRequest);

    void deleteFeed(UUID feedId);

    FeedResponseDto updateFeed(UUID feedId, UUID userId, FeedUpdateRequestDto feedUpdateRequestDto);

    void likeFeed(UUID feedId, UUID userId);

    void unlikeFeed(UUID feedId, UUID userId);

}
