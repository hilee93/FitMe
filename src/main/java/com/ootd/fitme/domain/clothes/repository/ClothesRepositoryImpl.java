package com.ootd.fitme.domain.clothes.repository;

import com.ootd.fitme.domain.attribute.entity.QAttribute;
import com.ootd.fitme.domain.clothes.dto.ClothesAttributeWithDefDto;
import com.ootd.fitme.domain.clothes.dto.ClothesDto;
import com.ootd.fitme.domain.clothes.dto.request.ClothesDtoCursorRequest;
import com.ootd.fitme.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothes.enums.SortBy;
import com.ootd.fitme.domain.clothes.enums.SortDirection;
import com.ootd.fitme.domain.clothesattribute.entity.ClothesAttribute;
import com.ootd.fitme.domain.clothesattributeselectablevalue.entity.QClothesAttributeSelectableValue;
import com.ootd.fitme.domain.selectablevalue.entity.QSelectableValue;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ootd.fitme.domain.clothes.entity.QClothes.clothes;
import static com.ootd.fitme.domain.clothesattribute.entity.QClothesAttribute.clothesAttribute;

@Repository
@RequiredArgsConstructor
public class ClothesRepositoryImpl implements ClothesRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final String CURSOR_DELIMITER = "|";

    @Override
    public ClothesDtoCursorResponse findClothesByCursor(ClothesDtoCursorRequest request) {
        int limit = request.limit() > 0 ? request.limit() : 20;

        List<Clothes> clothesList = queryFactory
                .selectFrom(clothes)
                .where(
                        ownerIdEq(request.ownerId()),
                        typeEq(request.typeEqual()),
                        cursorCondition(request)
                )
                .orderBy(getOrderSpecifiers(request))
                .limit(limit + 1)
                .fetch();

        boolean hasNext = clothesList.size() > limit;
        if (hasNext) {
            clothesList.remove(limit);
        }

        Long totalCount = queryFactory
                .select(clothes.count())
                .from(clothes)
                .where(
                        ownerIdEq(request.ownerId()),
                        typeEq(request.typeEqual())
                )
                .fetchOne();

        List<UUID> clothesIds = clothesList.stream()
                .map(Clothes::getId)
                .toList();

        QAttribute qAttribute = QAttribute.attribute;
        QClothesAttributeSelectableValue qCasv = QClothesAttributeSelectableValue.clothesAttributeSelectableValue;
        QSelectableValue qSelectableValue = QSelectableValue.selectableValue;

        List<ClothesAttribute> fetchedAttributes = clothesIds.isEmpty() ? List.of() : queryFactory
                .selectFrom(clothesAttribute)
                .join(clothesAttribute.attribute, qAttribute).fetchJoin()
                .join(clothesAttribute.clothesAttributeSelectableValue, qCasv).fetchJoin()
                .join(qCasv.selectableValue, qSelectableValue).fetchJoin()
                .where(clothesAttribute.clothes.id.in(clothesIds))
                .fetch();

        Map<UUID, List<ClothesAttribute>> attributeMap = fetchedAttributes.stream()
                .collect(Collectors.groupingBy(attr -> attr.getClothes().getId()));

        List<ClothesDto> contents = clothesList.stream().map(c -> {
            List<ClothesAttribute> attrs = attributeMap.getOrDefault(c.getId(), List.of());
            List<ClothesAttributeWithDefDto> attrDtos = attrs.stream()
                    .map(attr -> new ClothesAttributeWithDefDto(
                            attr.getAttribute().getId(),
                            attr.getAttribute().getName(),
                            null,
                            attr.getClothesAttributeSelectableValue().getSelectableValue().getType()
                    )).toList();

            return new ClothesDto(
                    c.getId(),
                    c.getUser().getId(),
                    c.getName(),
                    c.getImageUrl(),
                    c.getClothesType(),
                    attrDtos
            );
        }).toList();

        String nextCursor = null;
        String nextIdAfter = null;
        if (!contents.isEmpty()) {
            Clothes lastClothes = clothesList.get(clothesList.size() - 1);
            nextIdAfter = lastClothes.getId().toString();
            nextCursor = generateNextCursor(lastClothes, request.sortBy());
        }

        return new ClothesDtoCursorResponse(
                contents,
                nextCursor,
                nextIdAfter,
                hasNext,
                totalCount != null ? totalCount : 0L,
                request.sortBy() != null ? request.sortBy() : SortBy.createdAt,
                request.sortDirection() != null ? request.sortDirection() : SortDirection.DESCENDING
        );
    }


    private BooleanExpression ownerIdEq(String ownerId) {
        return ownerId != null && !ownerId.isBlank() ? clothes.user.id.eq(UUID.fromString(ownerId)) : null;
    }

    private BooleanExpression typeEq(com.ootd.fitme.domain.clothes.enums.ClothesType type) {
        return type != null ? clothes.clothesType.eq(type) : null;
    }

    /**
     * 커서 조건 생성 (기본 기획: 생성일 최신순 -> 이름 가나다순 -> ID)
     */
    private BooleanExpression cursorCondition(ClothesDtoCursorRequest request) {
        if (request.cursor() == null || request.cursor().isBlank() || request.idAfter() == null) {
            return null; // 첫 페이지
        }

        String[] parts = request.cursor().split("\\" + CURSOR_DELIMITER);
        Instant cursorTime = Instant.parse(parts[0]);
        String cursorName = parts.length > 1 ? parts[1] : "";
        UUID cursorId = UUID.fromString(request.idAfter());

        SortBy sortBy = request.sortBy() != null ? request.sortBy() : SortBy.createdAt;

        BooleanExpression timeLt = clothes.createdAt.lt(cursorTime);
        BooleanExpression timeEq = clothes.createdAt.eq(cursorTime);
        BooleanExpression timeGt = clothes.createdAt.gt(cursorTime);

        BooleanExpression nameGt = clothes.name.gt(cursorName);
        BooleanExpression nameEq = clothes.name.eq(cursorName);

        BooleanExpression idGt = clothes.id.gt(cursorId);

        if (sortBy == SortBy.createdAt) {
            return timeLt
                    .or(timeEq.and(nameGt))
                    .or(timeEq.and(nameEq).and(idGt));
        } else {
            return clothes.name.gt(cursorName)
                    .or(clothes.name.eq(cursorName).and(timeLt))
                    .or(clothes.name.eq(cursorName).and(timeEq).and(idGt));
        }
    }

    /**
     * 정렬 조건 (ORDER BY) 생성
     */
    private OrderSpecifier<?>[] getOrderSpecifiers(ClothesDtoCursorRequest request) {
        SortBy sortBy = request.sortBy() != null ? request.sortBy() : SortBy.createdAt;

        OrderSpecifier<Instant> timeDesc = new OrderSpecifier<>(Order.DESC, clothes.createdAt);
        OrderSpecifier<String> nameAsc = new OrderSpecifier<>(Order.ASC, clothes.name);
        OrderSpecifier<UUID> idAsc = new OrderSpecifier<>(Order.ASC, clothes.id); // Hidden Tie-breaker

        if (sortBy == SortBy.createdAt) {
            return new OrderSpecifier[]{timeDesc, nameAsc, idAsc};
        } else {
            return new OrderSpecifier[]{nameAsc, timeDesc, idAsc};
        }
    }

    /**
     * 다음 페이지 조회를 위한 커서 문자열 생성
     */
    private String generateNextCursor(Clothes clothes, SortBy sortBy) {
        return clothes.getCreatedAt().toString() + CURSOR_DELIMITER + clothes.getName();
    }
}