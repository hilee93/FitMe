package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.feed.dto.response.FeedAuthorSummaryDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface FeedProfileQueryRepository {
    Map<UUID, FeedAuthorSummaryDto> findAuthorsByUserIds(List<UUID> userIds);
}
