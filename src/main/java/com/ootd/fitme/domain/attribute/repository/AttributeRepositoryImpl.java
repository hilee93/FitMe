package com.ootd.fitme.domain.attribute.repository;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ootd.fitme.domain.attribute.entity.QAttribute.attribute;
import static com.ootd.fitme.domain.selectablevalue.entity.QSelectableValue.selectableValue;

@Repository
@RequiredArgsConstructor
public class AttributeRepositoryImpl implements AttributeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Attribute> findAttributesWithCondition(String sortBy, String sortDirection, String keywordLike) {
        return queryFactory
                .selectFrom(attribute).distinct()
                .leftJoin(attribute.selectableValues, selectableValue).fetchJoin()
                .where(containsKeyword(keywordLike))
                .orderBy(createOrderSpecifier(sortBy, sortDirection))
                .fetch();
    }

    private BooleanExpression containsKeyword(String keywordLike) {
        if (keywordLike == null || keywordLike.isBlank()) {
            return null;
        }
        return attribute.name.containsIgnoreCase(keywordLike);
    }
    private OrderSpecifier<?> createOrderSpecifier(String sortBy, String sortDirection) {
        boolean isAsc = "ASC".equalsIgnoreCase(sortDirection);

        if ("name".equalsIgnoreCase(sortBy)) {
            return isAsc ? attribute.name.asc() : attribute.name.desc();
        }

        return isAsc ? attribute.createdAt.asc() : attribute.createdAt.desc();
    }
}