package com.ootd.fitme.domain.follow.repository;

import com.ootd.fitme.domain.follow.dto.response.UserSummary;

import java.util.UUID;

public interface FollowProfileQueryRepository {

    // 증감 메서드
    void increaseFollowerCount(UUID userId);
    void decreaseFollowerCount(UUID userId);
    void increaseFolloweeCount(UUID userId);
    void decreaseFolloweeCount(UUID userId);

    // profile 카운트 바로 조회
    int findFollowerCountByUserId(UUID userId);
    int findFolloweeCountByUserId(UUID userId);

    UserSummary findUserSummaryByUserId(UUID userId);
}
