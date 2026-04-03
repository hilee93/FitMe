package com.ootd.fitme.domain.feed.dto.request;

import com.ootd.fitme.domain.feed.enums.FeedSortCriteria;
import com.ootd.fitme.domain.feed.enums.SortDirection;
import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FeedSearchCondition(
        String keywordLike,
        @NotNull FeedSortCriteria sortBy,
        @NotNull SortDirection sortDirection,
        String cursor,
        UUID idAfter,
        @NotNull Integer limit,
        SkyStatus skyStatusEqual,
        PrecipitationType precipitationTypeEqual,
        UUID authorIdEqual
) {
}
