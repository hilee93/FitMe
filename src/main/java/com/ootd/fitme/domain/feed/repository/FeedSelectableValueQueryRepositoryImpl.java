package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.attribute.entity.QAttribute;
import com.ootd.fitme.domain.clothesattribute.entity.QClothesAttribute;
import com.ootd.fitme.domain.clothesattributeselectablevalue.entity.QClothesAttributeSelectableValue;
import com.ootd.fitme.domain.feed.dto.response.AttributeSelectableValueRow;
import com.ootd.fitme.domain.selectablevalue.entity.QSelectableValue;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FeedSelectableValueQueryRepositoryImpl implements FeedSelectableValueQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<UUID, List<String>> findFeedSelectableValuesByAttributeIds(List<UUID> attributeIds) {

        QClothesAttributeSelectableValue casv = QClothesAttributeSelectableValue.clothesAttributeSelectableValue;
        QSelectableValue selectableValue = QSelectableValue.selectableValue;
        QClothesAttribute clothesAttribute = QClothesAttribute.clothesAttribute;
        QAttribute attribute = QAttribute.attribute;

        List<AttributeSelectableValueRow> rows = queryFactory.select(
                        Projections.constructor(
                                AttributeSelectableValueRow.class,
                                attribute.id,
                                selectableValue.type
                        ))
                .from(casv)
                .join(casv.selectableValue, selectableValue)
                .join(casv.clothesAttribute, clothesAttribute)
                .join(clothesAttribute.attribute, attribute)
                .where(attribute.id.in(attributeIds))
                .fetch();

        return rows.stream()
                .collect(Collectors.groupingBy(
                        AttributeSelectableValueRow::attributeDefinitionId,
                        Collectors.mapping(
                                AttributeSelectableValueRow::value,
                                Collectors.toList()
                        )
                ));
    }
}