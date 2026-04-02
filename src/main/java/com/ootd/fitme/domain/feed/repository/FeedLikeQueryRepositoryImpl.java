package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.feedlike.entity.QFeedLike;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.ootd.fitme.domain.feedlike.entity.QFeedLike.feedLike;

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

    @Override
    public Set<UUID> findLikedByFeedIds(List<UUID> feedIds, UUID userId) {
        if (feedIds == null || feedIds.isEmpty()) {
            return Set.of();
        }

        List<UUID> likedFeedIds = queryFactory
                .select(feedLike.feed.id)
                .from(feedLike)
                .where(
                        feedLike.feed.id.in(feedIds),
                        feedLike.user.id.eq(userId)
                )
                .fetch();

        return new HashSet<>(likedFeedIds);
    }
}
