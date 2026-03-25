package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.feed.dto.response.FeedClothesFlatRow;

import java.util.List;
import java.util.UUID;

public interface FeedClothesQueryRepository {
    List<FeedClothesFlatRow> findFeedClothes(UUID feedId);
}
