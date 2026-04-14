package com.ootd.fitme.domain.feed.repository.elasticsearch;

import java.util.UUID;

public interface FeedDocumentCustomRepository {
    void updateLikeCount(UUID feedId, int likeCount);
}
