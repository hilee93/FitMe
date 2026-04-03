package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.feed.dto.request.FeedSearchCondition;
import com.ootd.fitme.domain.feed.dto.response.CursorResult;
import com.ootd.fitme.domain.feed.dto.response.FeedBaseFlatRow;
import com.ootd.fitme.domain.feed.dto.response.FeedDetailFlatRow;
import com.ootd.fitme.domain.feed.enums.FeedSortCriteria;
import com.ootd.fitme.domain.feed.enums.SortDirection;
import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
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

    @Override
    public CursorResult<FeedBaseFlatRow> findFeedListFlatRows(FeedSearchCondition condition) {

        int size = condition.limit();

        List<FeedBaseFlatRow> feedBaseFlatRows = queryFactory.select(
                        Projections.constructor(
                                FeedBaseFlatRow.class,
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
                .where(
                        keywordLike(condition.keywordLike()),
                        skyStatusEq(condition.skyStatusEqual()),
                        precipitationTypeEq(condition.precipitationTypeEqual()),
                        authorIdEq(condition.authorIdEqual()),
                        cursorPredicate(condition)
                )
                .orderBy(
                        getPrimaryOrderSpecifier(condition.sortBy(), condition.sortDirection()),
                        feed.id.asc()
                )
                .limit(size + 1)
                .fetch();

        boolean hasNext = feedBaseFlatRows.size() > size;

        if (hasNext) {
            feedBaseFlatRows.remove(size);
        }

        long total = countFeeds();

        return new CursorResult<>(
                feedBaseFlatRows,
                hasNext,
                total
        );

    }


    private BooleanExpression keywordLike(String keywordLike) {
        if (keywordLike == null || keywordLike.isBlank()) {
            return null;
        }
        return feed.content.containsIgnoreCase(keywordLike);
    }

    private BooleanExpression skyStatusEq(SkyStatus skyStatus) {
        if (skyStatus == null) {
            return null;
        }
        return weatherForecast.skyStatus.eq(skyStatus);
    }

    private BooleanExpression precipitationTypeEq(PrecipitationType precipitationType) {
        if (precipitationType == null) {
            return null;
        }
        return weatherForecast.precipitationType.eq(precipitationType);
    }

    private BooleanExpression authorIdEq(UUID authorId) {
        if (authorId == null) {
            return null;
        }
        return feed.user.id.eq(authorId);
    }

    private BooleanExpression cursorPredicate(FeedSearchCondition condition) {
        if (condition.cursor() == null || condition.idAfter() == null) {
            return null;
        }

        boolean isAsc = condition.sortDirection() == SortDirection.ASCENDING;
        UUID idAfter = condition.idAfter();

        return switch (condition.sortBy()) {
            case CREATED_AT -> {
                Instant cursor = Instant.parse(condition.cursor());

                yield isAsc ? feed.createdAt.gt(cursor)
                        .or(feed.createdAt.eq(cursor).and(feed.id.gt(idAfter)))
                        : feed.createdAt.lt(cursor)
                        .or(feed.createdAt.eq(cursor).and(feed.id.lt(idAfter)));
            }
            case LIKE_COUNT -> {

                int cursor = Integer.parseInt(condition.cursor());

                yield isAsc
                        ? feed.likeCount.gt(cursor)
                        .or(feed.likeCount.eq(cursor).and(feed.id.gt(idAfter)))
                        : feed.likeCount.lt(cursor)
                        .or(feed.likeCount.eq(cursor).and(feed.id.gt(idAfter)));
            }
        };

    }

    private long countFeeds() {
        Long total = queryFactory.select(feed.count())
                .from(feed)
                .fetchOne();

        return total == null ? 0 : total;
    }

    private OrderSpecifier<?> getPrimaryOrderSpecifier(FeedSortCriteria sortBy, SortDirection sortDirection) {
        boolean isAsc = sortDirection == SortDirection.ASCENDING;

        return switch (sortBy) {
            case CREATED_AT -> isAsc ? feed.createdAt.asc() : feed.createdAt.desc();
            case LIKE_COUNT -> isAsc ? feed.likeCount.asc() : feed.likeCount.desc();
        };

    }

}
