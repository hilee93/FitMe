package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.feed.dto.response.FeedAuthorSummaryDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ootd.fitme.domain.profile.entity.QProfile.profile;
import static com.ootd.fitme.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class FeedProfileQueryRepositoryImpl implements FeedProfileQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<UUID, FeedAuthorSummaryDto> findAuthorsByUserIds(List<UUID> userIds) {

        List<FeedAuthorSummaryDto> results = queryFactory.select(
                        Projections.constructor(
                                FeedAuthorSummaryDto.class,
                                user.id,
                                profile.name,
                                profile.profileImageUrl
                        )
                )
                .from(profile)
                .join(profile.user, user)
                .where(user.id.in(userIds))
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        FeedAuthorSummaryDto::userId,
                        summaryDto -> summaryDto
                ));
    }
}
