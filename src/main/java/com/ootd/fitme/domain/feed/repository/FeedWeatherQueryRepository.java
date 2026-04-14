package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.feed.dto.response.FeedWeatherSummaryDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface FeedWeatherQueryRepository {
    Map<UUID, FeedWeatherSummaryDto> findWeatherSummaryByIds(List<UUID> weatherIds);
}
