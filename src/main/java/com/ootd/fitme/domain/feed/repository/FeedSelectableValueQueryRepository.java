package com.ootd.fitme.domain.feed.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface FeedSelectableValueQueryRepository {
    Map<UUID, List<String>> findFeedSelectableValuesByAttributeIds(List<UUID> attributeIds);
}