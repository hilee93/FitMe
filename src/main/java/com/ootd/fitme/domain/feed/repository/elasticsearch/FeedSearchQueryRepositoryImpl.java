package com.ootd.fitme.domain.feed.repository.elasticsearch;

import com.ootd.fitme.domain.feed.dto.request.FeedSearchCondition;
import com.ootd.fitme.domain.feed.dto.response.CursorResult;
import com.ootd.fitme.domain.feed.dto.response.elasticsearch.FeedSearchHitRow;
import org.springframework.stereotype.Repository;

@Repository
public class FeedSearchQueryRepositoryImpl implements FeedSearchQueryRepository {

    @Override
    public CursorResult<FeedSearchHitRow> searchFeeds(FeedSearchCondition condition) {
    // TODO: Operations
        return null;
    }


}
