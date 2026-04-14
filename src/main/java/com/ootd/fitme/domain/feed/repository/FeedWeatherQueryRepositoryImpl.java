package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.feed.dto.response.FeedPrecipitationSummaryDto;
import com.ootd.fitme.domain.feed.dto.response.FeedTemperatureSummaryDto;
import com.ootd.fitme.domain.feed.dto.response.FeedWeatherFlatRow;
import com.ootd.fitme.domain.feed.dto.response.FeedWeatherSummaryDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ootd.fitme.domain.weatherforecast.entity.QWeatherForecast.weatherForecast;

@Repository
@RequiredArgsConstructor
public class FeedWeatherQueryRepositoryImpl implements FeedWeatherQueryRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<UUID, FeedWeatherSummaryDto> findWeatherSummaryByIds(List<UUID> weatherIds) {

        if (weatherIds == null || weatherIds.isEmpty()) {
            return Map.of();
        }

        List<FeedWeatherFlatRow> rows = queryFactory
                .select(
                        Projections.constructor(
                                FeedWeatherFlatRow.class,
                                weatherForecast.id,
                                weatherForecast.skyStatus,
                                weatherForecast.precipitationType,
                                weatherForecast.precipitationAmount,
                                weatherForecast.precipitationProbability,
                                weatherForecast.temperatureCurrent,
                                weatherForecast.temperatureComparedToDayBefore,
                                weatherForecast.temperatureMin,
                                weatherForecast.temperatureMax
                        )
                )
                .from(weatherForecast)
                .where(weatherForecast.id.in(weatherIds))
                .fetch();

        return rows.stream()
                .collect(Collectors.toMap(
                        FeedWeatherFlatRow::weatherId,
                        row -> new FeedWeatherSummaryDto(
                                row.weatherId(),
                                row.skyStatus(),
                                new FeedPrecipitationSummaryDto(
                                        row.precipitationType(),
                                        row.precipitationAmount(),
                                        row.precipitationProbability()
                                ),
                                new FeedTemperatureSummaryDto(
                                        row.temperatureCurrent(),
                                        row.temperatureComparedToDayBefore(),
                                        row.temperatureMin(),
                                        row.temperatureMax()
                                )
                        )
                ));
    }
}
