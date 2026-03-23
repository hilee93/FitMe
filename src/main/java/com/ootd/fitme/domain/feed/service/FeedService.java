package com.ootd.fitme.domain.feed.service;

import com.ootd.fitme.domain.feed.dto.request.FeedSearchCondition;
import com.ootd.fitme.domain.feed.dto.response.FeedCursorResponseDto;

public interface FeedService {
    FeedCursorResponseDto searchFeed(FeedSearchCondition feedSearchCondition );
    // TODO: 필요 기능 메서드 추가
}
