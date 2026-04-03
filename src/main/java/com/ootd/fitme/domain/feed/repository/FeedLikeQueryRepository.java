package com.ootd.fitme.domain.feed.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface FeedLikeQueryRepository {
    boolean existsLike(UUID feedId, UUID userId);

    Set<UUID> findLikedByFeedIds(List<UUID> feedIds, UUID userId);
}
