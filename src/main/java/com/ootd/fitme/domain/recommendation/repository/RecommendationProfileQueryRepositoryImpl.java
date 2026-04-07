package com.ootd.fitme.domain.recommendation.repository;

import com.ootd.fitme.domain.recommendation.dto.response.RecommendationProfileSummaryDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import static com.ootd.fitme.domain.profile.entity.QProfile.profile;
import static com.ootd.fitme.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class RecommendationProfileQueryRepositoryImpl implements RecommendationProfileQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<RecommendationProfileSummaryDto> findProfileByUserId(UUID userId) {
        RecommendationProfileSummaryDto result = queryFactory
                .select(Projections.constructor(
                        RecommendationProfileSummaryDto.class,
                        profile.gender.stringValue(),
                        profile.temperatureSensitivity.intValue()
                ))
                .from(profile)
                .join(profile.user, user)
                .where(user.id.eq(userId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
