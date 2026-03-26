package com.ootd.fitme.domain.feed.repository;

import java.util.UUID;

public interface FeedLikeQueryRepository {
    boolean existsLike(UUID feedId, UUID userId);
}
