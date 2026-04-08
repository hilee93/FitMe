package com.ootd.fitme.domain.follow.repository;

import com.ootd.fitme.domain.follow.dto.response.UserSummary;
import com.ootd.fitme.domain.profile.entity.QProfile;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FollowProfileQueryRepositoryImpl implements FollowProfileQueryRepository {

    private final JPAQueryFactory query;
    private final QProfile profile = QProfile.profile;

    @Override
    public void increaseFollowerCount(UUID userId) {
        query.update(profile)
                .set(profile.followerCount, profile.followerCount.add(1))
                .where(profile.user.id.eq(userId))
                .execute();
    }

    @Override
    public void decreaseFollowerCount(UUID userId) {
        query.update(profile)
                .set(profile.followerCount, profile.followerCount.subtract(1))
                .where(profile.user.id.eq(userId).and(profile.followerCount.gt(0)))
                .execute();
    }

    @Override
    public void increaseFolloweeCount(UUID userId) {
        query.update(profile)
                .set(profile.followeeCount, profile.followeeCount.add(1))
                .where(profile.user.id.eq(userId))
                .execute();
    }

    @Override
    public void decreaseFolloweeCount(UUID userId) {
        query.update(profile)
                .set(profile.followeeCount, profile.followeeCount.subtract(1))
                .where(profile.user.id.eq(userId).and(profile.followeeCount.gt(0)))
                .execute();
    }

    @Override
    public int findFollowerCountByUserId(UUID userId) {
        Integer count = query.select(profile.followerCount)
                .from(profile)
                .where(profile.user.id.eq(userId))
                .fetchOne();
        return count != null ? count : 0;
    }

    @Override
    public int findFolloweeCountByUserId(UUID userId) {
        Integer count = query.select(profile.followeeCount)
                .from(profile)
                .where(profile.user.id.eq(userId))
                .fetchOne();
        return count != null ? count : 0;
    }

    @Override
    public UserSummary findUserSummaryByUserId(UUID userId) {
        return query.select(Projections.constructor(UserSummary.class,
                profile.user.id,
                profile.name,
                profile.profileImageUrl))
                .from(profile)
                .where(profile.user.id.eq(userId))
                .fetchOne();
    }
}
