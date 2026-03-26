package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.attribute.entity.QAttribute;
import com.ootd.fitme.domain.clothes.entity.QClothes;
import com.ootd.fitme.domain.clothesattribute.entity.QClothesAttribute;
import com.ootd.fitme.domain.clothesattributeselectablevalue.entity.QClothesAttributeSelectableValue;
import com.ootd.fitme.domain.feed.dto.response.FeedClothesFlatRow;
import com.ootd.fitme.domain.feedclothes.entity.QFeedClothes;
import com.ootd.fitme.domain.selectablevalue.entity.QSelectableValue;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FeedClothesQueryRepositoryImpl implements FeedClothesQueryRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<FeedClothesFlatRow> findFeedClothes(UUID feedId) {

        QFeedClothes feedClothes = QFeedClothes.feedClothes;
        QClothes clothes = QClothes.clothes;
        QClothesAttribute clothesAttribute = QClothesAttribute.clothesAttribute;
        QAttribute attribute = QAttribute.attribute;
        QClothesAttributeSelectableValue clothesAttributeSelectableValue = QClothesAttributeSelectableValue.clothesAttributeSelectableValue;
        QSelectableValue selectableValue = QSelectableValue.selectableValue;
        return queryFactory
                .select(Projections.constructor(
                        FeedClothesFlatRow.class,
                        clothes.id,
                        clothes.name,
                        clothes.imageUrl,
                        clothes.clothesType,
                        attribute.id,
                        attribute.name,
                        selectableValue.type
                ))
                .from(feedClothes)
                .join(feedClothes.clothes, clothes)
                .leftJoin(clothesAttribute).on(clothesAttribute.clothes.eq(clothes))
                .leftJoin(clothesAttribute.attribute, attribute)
                .leftJoin(clothesAttributeSelectableValue).on(clothesAttributeSelectableValue.clothesAttribute.eq(clothesAttribute))
                .leftJoin(clothesAttributeSelectableValue.selectableValue, selectableValue)
                .where(feedClothes.feed.id.eq(feedId))
                .fetch();
    }
}
