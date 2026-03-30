package com.ootd.fitme.domain.feed.dto.request;

import com.ootd.fitme.domain.feed.enums.FeedSortCriteria;
import com.ootd.fitme.domain.feed.enums.SortDirection;
import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;

public record FeedSearchCondition(
        String keywordLike,
        FeedSortCriteria sortBy,
        SortDirection sortDirection,
        String cursor,
        String idAfter,
        Integer limit,
        SkyStatus skyStatusEqual,
        PrecipitationType precipitationTypeEqual,
        String authorIdEqual
) {
    public FeedSearchCondition {
        if (sortDirection == null) sortDirection = SortDirection.DESCENDING;
        if (limit == null || limit <= 0) limit = 10;
    }
}
