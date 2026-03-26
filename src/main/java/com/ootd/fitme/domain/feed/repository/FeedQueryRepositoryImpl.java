package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.feed.dto.response.FeedDetailFlatRow;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import static com.ootd.fitme.domain.feed.entity.QFeed.feed;
import static com.ootd.fitme.domain.user.entity.QUser.user;
import static com.ootd.fitme.domain.weatherforecast.entity.QWeatherForecast.weatherForecast;

@Repository
@RequiredArgsConstructor
public class FeedQueryRepositoryImpl implements FeedQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<FeedDetailFlatRow> findFeedDetail(UUID feedId) {

        FeedDetailFlatRow result = queryFactory.select(
                        Projections.constructor(
                                FeedDetailFlatRow.class,
                                feed.id,
                                feed.createdAt,
                                feed.updatedAt,
                                feed.content,
                                feed.likeCount,
                                feed.commentCount,
                                user.id,
                                weatherForecast.id,
                                weatherForecast.skyStatus,
                                weatherForecast.precipitationType,
                                weatherForecast.precipitationAmount,
                                weatherForecast.precipitationProbability,
                                weatherForecast.temperatureCurrent,
                                weatherForecast.temperatureComparedToDayBefore,
                                weatherForecast.temperatureMin,
                                weatherForecast.temperatureMax
                        ))
                .from(feed)
                .join(feed.user, user)
                .join(feed.weatherForecast, weatherForecast)
                .where(feed.id.eq(feedId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
