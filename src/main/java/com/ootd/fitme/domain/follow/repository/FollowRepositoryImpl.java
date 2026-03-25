package com.ootd.fitme.domain.follow.repository;

import com.ootd.fitme.domain.follow.dto.response.FollowCursorDto;
import com.ootd.fitme.domain.follow.dto.response.UserSummary;
import com.ootd.fitme.domain.follow.entity.QFollow;
import com.ootd.fitme.domain.profile.entity.QProfile;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<FollowCursorDto> findFollowers(UUID followeeId, String cursor, UUID idAfter, int limit, String nameLike) {

        QFollow follow = QFollow.follow;
        QProfile followerProfile = new QProfile("followerProfile");

        return findFollowList(
                follow.followeeId.eq(followeeId),
                followerProfile,
                cursor, idAfter, limit, nameLike
        );
    }

    @Override
    public List<FollowCursorDto> findFollowings(UUID followerId, String cursor, UUID idAfter, int limit, String nameLike) {

        QFollow follow = QFollow.follow;
        QProfile followeeProfile = new QProfile("followeeProfile");

        return findFollowList(
                follow.followerId.eq(followerId),
                followeeProfile,
                cursor, idAfter, limit, nameLike
        );
    }

    private List<FollowCursorDto> findFollowList(
            BooleanExpression condition, QProfile nameSearchProfile, String cursor, UUID idAfter, int limit, String nameLike) {
        QFollow follow = QFollow.follow;
        QProfile followeeProfile = new QProfile("followeeProfile");
        QProfile followerProfile = new QProfile("followerProfile");

        return jpaQueryFactory
                .select(
                        Projections.constructor(FollowCursorDto.class,
                                follow.id,
                                Projections.constructor(UserSummary.class,
                                        follow.followeeId,
                                        followeeProfile.name,
                                        followeeProfile.profileImageUrl
                                ),
                                Projections.constructor(UserSummary.class,
                                        follow.followerId,
                                        followerProfile.name,
                                        followerProfile.profileImageUrl
                                ),
                                follow.createdAt
                        )
                )
                .from(follow)
                .join(followeeProfile).on(followeeProfile.user.id.eq(follow.followeeId))
                .join(followerProfile).on(followerProfile.user.id.eq(follow.followerId))
                .where(
                        condition,
                        nameLikeCondition(nameSearchProfile, nameLike),
                        cursorCondition(cursor, idAfter)
                )
                .orderBy(follow.createdAt.desc(), follow.id.asc())
                .limit(limit + 1)
                .fetch();
    }

    private BooleanExpression nameLikeCondition(QProfile profile, String nameLike) {
        if (nameLike == null || nameLike.isBlank()) return null;
        return profile.name.containsIgnoreCase(nameLike);
    }

    private BooleanExpression cursorCondition(String cursor, UUID idAfter) {
        if (cursor == null) return null;
        Instant nextCursor = Instant.parse(cursor);
        BooleanExpression condition = QFollow.follow.createdAt.lt(nextCursor);
        if (idAfter != null) {
            condition = condition.or(QFollow.follow.createdAt.eq(nextCursor).and(QFollow.follow.id.gt(idAfter)));
        }
        return condition;
    }
}
