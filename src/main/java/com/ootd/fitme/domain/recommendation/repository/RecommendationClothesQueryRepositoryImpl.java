package com.ootd.fitme.domain.recommendation.repository;

import com.ootd.fitme.domain.clothes.entity.QClothes;
import com.ootd.fitme.domain.recommendation.dto.response.RecommendationClothesSummaryDto;
import com.ootd.fitme.domain.user.entity.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RecommendationClothesQueryRepositoryImpl implements RecommendationClothesQueryRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<RecommendationClothesSummaryDto> findClothesByUserId(UUID userId) {
        QClothes clothes = QClothes.clothes;
        QUser user = QUser.user;

        return queryFactory
                .select(Projections.constructor(
                        RecommendationClothesSummaryDto.class,
                        clothes.id,
                        clothes.name,
                        clothes.clothesType,
                        clothes.imageUrl,
                        Expressions.constant(List.of())
                ))
                .from(clothes)
                .join(clothes.user, user)
                .where(user.id.eq(userId))
                .fetch();
    }

}
