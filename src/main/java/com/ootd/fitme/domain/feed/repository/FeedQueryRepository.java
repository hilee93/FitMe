package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.feed.dto.request.FeedSearchCondition;
import com.ootd.fitme.domain.feed.dto.response.CursorResult;
import com.ootd.fitme.domain.feed.dto.response.FeedBaseFlatRow;
import com.ootd.fitme.domain.feed.dto.response.FeedDetailFlatRow;

import java.util.Optional;
import java.util.UUID;

public interface FeedQueryRepository {

    Optional<FeedDetailFlatRow> findFeedDetail(UUID feedId);

    CursorResult<FeedBaseFlatRow> findFeedListFlatRows(FeedSearchCondition condition);
}
