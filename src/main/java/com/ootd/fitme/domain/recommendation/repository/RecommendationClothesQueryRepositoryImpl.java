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
        QClothes clothes = QClothes.clothes;

        // 옷 기본 정보 조회
        List<RecommendationClothesSummaryDto> clothesList = queryFactory
                .select(Projections.constructor(
                        RecommendationClothesSummaryDto.class,
                        clothes.id,
                        clothes.name,
                        clothes.clothesType,
                        clothes.imageUrl,
                        Expressions.constant(Collections.<RecommendationClothAttributeSummaryDto>emptyList())
                ))
                .from(clothes)
                .where(clothes.user.id.eq(userId))
                .fetch();

        if (clothesList.isEmpty()) {
            return clothesList;
        }

        // 옷 id 집합
        Set<UUID> clothesIds = clothesList.stream()
                .map(RecommendationClothesSummaryDto::clothesId)
                .collect(Collectors.toSet());

        // 한 번에 해당 옷들의 속성을 다 조회해서 Map<clothesId, attributes> 로 변환
        Map<UUID, List<RecommendationClothAttributeSummaryDto>> attributesByClothesId =
                getAttributesForClothesIds(clothesIds);

        // DTO에 attributes 주입해서 반환
        return clothesList.stream()
                .map(c -> new RecommendationClothesSummaryDto(
                        c.clothesId(),
                        c.name(),
                        c.type(),
                        c.imageUrl(),
                        attributesByClothesId.getOrDefault(c.clothesId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    /**
     * 여러 개의 clothesId 에 대해, 각 옷의 속성 리스트를 한 번에 조회하여
     * Map<clothesId, List<RecommendationClothAttributeSummaryDto>> 로 반환.
     */
    private Map<UUID, List<RecommendationClothAttributeSummaryDto>> getAttributesForClothesIds(Set<UUID> clothesIds) {
        if (clothesIds == null || clothesIds.isEmpty()) {
            return Collections.emptyMap();
        }

        QClothes clothes = QClothes.clothes;
        QClothesAttribute clothesAttribute = QClothesAttribute.clothesAttribute;
        QAttribute attribute = QAttribute.attribute;
        QClothesAttributeSelectableValue clothesAttributeSelectableValue = QClothesAttributeSelectableValue.clothesAttributeSelectableValue;
        QSelectableValue selectableValue = QSelectableValue.selectableValue;

        //  선택된 속성 한 번에 조회
        List<Tuple> selectedAttributes = queryFactory
                .select(
                        clothes.id,
                        attribute.id,
                        attribute.name,
                        selectableValue.type.stringValue()
                )
                .from(clothes)
                .innerJoin(clothes.attributes, clothesAttribute)
                .innerJoin(clothesAttribute.attribute, attribute)
                .innerJoin(clothesAttribute.clothesAttributeSelectableValue, clothesAttributeSelectableValue)
                .innerJoin(clothesAttributeSelectableValue.selectableValue, selectableValue)
                .where(clothes.id.in(clothesIds))
                .fetch();

        if (selectedAttributes.isEmpty()) {
            return Collections.emptyMap();
        }

        // 관련 attribute id 모아서, 각 attribute 의 selectable 값 전체 조회
        Set<UUID> attributeIds = selectedAttributes.stream()
                .map(t -> t.get(attribute.id))
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
                        t -> t.get(selectableValue.attribute.id),
                        Collectors.mapping(
                                t -> t.get(selectableValue.type.stringValue()),
                                Collectors.toList()
                        )
                ));

        Map<UUID, List<RecommendationClothAttributeSummaryDto>> result = new HashMap<>();

        for (Tuple t : selectedAttributes) {
            UUID clothesId = t.get(clothes.id);
            UUID attrId = t.get(attribute.id);

            RecommendationClothAttributeSummaryDto dto =
                    new RecommendationClothAttributeSummaryDto(
                            attrId,
                            t.get(attribute.name),
                            allSelectableValues.getOrDefault(attrId, Collections.emptyList()),
                            t.get(selectableValue.type.stringValue())
                    );

            result.computeIfAbsent(clothesId, k -> new ArrayList<>()).add(dto);
        }

        return result;
    }

    @Override
    public List<RecommendationClothAttributeSummaryDto> getAttributesForClothes(UUID clothesId) {
        Map<UUID, List<RecommendationClothAttributeSummaryDto>> map =
                getAttributesForClothesIds(Set.of(clothesId));
        return map.getOrDefault(clothesId, Collections.emptyList());
    }
}