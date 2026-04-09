package com.ootd.fitme.domain.recommendation.repository;


import com.ootd.fitme.domain.attribute.entity.QAttribute;
import com.ootd.fitme.domain.clothes.entity.QClothes;
import com.ootd.fitme.domain.clothesattribute.entity.QClothesAttribute;
import com.ootd.fitme.domain.clothesattributeselectablevalue.entity.QClothesAttributeSelectableValue;
import com.ootd.fitme.domain.recommendation.dto.response.RecommendationClothAttributeSummaryDto;
import com.ootd.fitme.domain.recommendation.dto.response.RecommendationClothesSummaryDto;
import com.ootd.fitme.domain.selectablevalue.entity.QSelectableValue;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RecommendationClothesQueryRepositoryImpl implements RecommendationClothesQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<RecommendationClothesSummaryDto> findClothesByUserId(UUID userId) {
        // 옷 기본 정보만 조회
        List<RecommendationClothesSummaryDto> clothesWithoutAttributes = getClothesBasicInfo(userId);

        //  각 옷에 속성 정보 추가
        return clothesWithoutAttributes.stream()
                .map(this::addAttributesToClothes)
                .collect(Collectors.toList());
    }

    // 옷 기본 정보만 조회
    private List<RecommendationClothesSummaryDto> getClothesBasicInfo(UUID userId) {
        QClothes clothes = QClothes.clothes;

        return queryFactory
                .select(Projections.constructor(
                        RecommendationClothesSummaryDto.class,
                        clothes.id,
                        clothes.name,
                        clothes.clothesType,
                        clothes.imageUrl,
                        Expressions.constant(new ArrayList<RecommendationClothAttributeSummaryDto>())
                ))
                .from(clothes)
                .where(clothes.user.id.eq(userId))
                .fetch();
    }

    // 각 옷에 속성 정보 추가
    private RecommendationClothesSummaryDto addAttributesToClothes(RecommendationClothesSummaryDto clothes) {
        List<RecommendationClothAttributeSummaryDto> attributes = getAttributesForClothes(clothes.clothesId());

        return new RecommendationClothesSummaryDto(
                clothes.clothesId(),
                clothes.name(),
                clothes.type(),
                clothes.imageUrl(),
                attributes
        );
    }

    @Override
    public List<RecommendationClothAttributeSummaryDto> getAttributesForClothes(UUID clothesId) {
        QClothes clothes = QClothes.clothes;
        QClothesAttribute clothesAttribute = QClothesAttribute.clothesAttribute;
        QAttribute attribute = QAttribute.attribute;
        QClothesAttributeSelectableValue clothesAttributeSelectableValue = QClothesAttributeSelectableValue.clothesAttributeSelectableValue;
        QSelectableValue selectableValue = QSelectableValue.selectableValue;

        // 선택된 속성 조회
        List<Tuple> selectedAttributes = queryFactory
                .select(
                        attribute.id,
                        attribute.name,
                        selectableValue.type.stringValue()
                )
                .from(clothes)
                .innerJoin(clothes.attributes, clothesAttribute)
                .innerJoin(clothesAttribute.attribute, attribute)
                .innerJoin(clothesAttribute.clothesAttributeSelectableValue, clothesAttributeSelectableValue)
                .innerJoin(clothesAttributeSelectableValue.selectableValue, selectableValue)
                .where(clothes.id.eq(clothesId))
                .fetch();

        if (selectedAttributes.isEmpty()) {
            return Collections.emptyList();
        }

        // 각 속성의 모든 선택 가능한 값들 조회
        Set<UUID> attributeIds = selectedAttributes.stream()
                .map(tuple -> tuple.get(attribute.id))
                .collect(Collectors.toSet());

        Map<UUID, List<String>> allSelectableValues = queryFactory
                .select(
                        selectableValue.attribute.id,
                        selectableValue.type.stringValue()
                )
                .from(selectableValue)
                .where(selectableValue.attribute.id.in(attributeIds)
                        .and(selectableValue.isDeleted.eq(false)))
                .orderBy(selectableValue.displayOrder.asc())
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(selectableValue.attribute.id),
                        Collectors.mapping(
                                tuple -> tuple.get(selectableValue.type.stringValue()),
                                Collectors.toList()
                        )
                ));

        // DTO 생성
        return selectedAttributes.stream()
                .map(tuple -> {
                    UUID attrId = tuple.get(attribute.id);
                    return new RecommendationClothAttributeSummaryDto(
                            attrId,
                            tuple.get(attribute.name),
                            allSelectableValues.getOrDefault(attrId, Collections.emptyList()),
                            tuple.get(selectableValue.type.stringValue())
                    );
                })
                .collect(Collectors.toList());
    }
}