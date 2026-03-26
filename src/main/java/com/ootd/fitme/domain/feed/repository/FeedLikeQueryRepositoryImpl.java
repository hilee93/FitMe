package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.feedlike.entity.QFeedLike;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FeedLikeQueryRepositoryImpl implements FeedLikeQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsLike(UUID feedId, UUID userId) {

        QFeedLike feedLike = QFeedLike.feedLike;

        Integer result = queryFactory.selectOne()
                .from(feedLike)
                .where(
                        feedLike.feed.id.eq(feedId),
                        feedLike.user.id.eq(userId)
                )
                .fetchFirst();

        return result != null;
    }
}
