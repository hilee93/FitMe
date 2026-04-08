package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.feed.dto.response.FeedClothesFlatRow;
import com.ootd.fitme.domain.feed.dto.response.FeedListClothesFlatRow;

import java.util.List;
import java.util.UUID;

public interface FeedClothesQueryRepository {
    List<FeedClothesFlatRow> findFeedClothes(UUID feedId);

    List<FeedListClothesFlatRow> findFeedClothesByFeedIds(List<UUID> feedIds);
}
