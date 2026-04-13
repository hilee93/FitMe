package com.ootd.fitme.domain.feed.repository.elasticsearch;

import com.ootd.fitme.domain.feed.dto.request.FeedSearchCondition;
import com.ootd.fitme.domain.feed.dto.response.CursorResult;
import com.ootd.fitme.domain.feed.dto.response.elasticsearch.FeedSearchHitRow;

public interface FeedSearchQueryRepository {

    CursorResult<FeedSearchHitRow> searchFeeds(FeedSearchCondition condition);

}
