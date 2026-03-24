package com.ootd.fitme.domain.feed.repository;

import java.util.Optional;
import java.util.UUID;

public interface FeedQueryRepository {

    Optional<FeedDetailFlatRow> findFeedDetail(UUID feedId);
}
