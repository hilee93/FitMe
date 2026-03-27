package com.ootd.fitme.domain.follow.repository;

import java.util.UUID;

public interface FollowProfileQueryRepository {

    void increaseFollowerCount(UUID userId);
    void decreaseFollowerCount(UUID userId);
    void increaseFolloweeCount(UUID userId);
    void decreaseFolloweeCount(UUID userId);

    int findFollowerCountByUserId(UUID userId);
    int findFolloweeCountByUserId(UUID userId);
}
