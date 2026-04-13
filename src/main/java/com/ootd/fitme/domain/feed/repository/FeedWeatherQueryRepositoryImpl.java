package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.feed.dto.response.FeedWeatherSummaryDto;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class FeedWeatherQueryRepositoryImpl implements FeedWeatherQueryRepository{
    @Override
    public Map<UUID, FeedWeatherSummaryDto> findWeatherSummaryByIds(List<UUID> weatherIds) {
        return Map.of();
    }
}
