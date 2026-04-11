package com.ootd.fitme.domain.user.repository;

import com.ootd.fitme.domain.profile.entity.QProfile;
import com.ootd.fitme.domain.user.dto.request.UserSearchCondition;
import com.ootd.fitme.domain.user.dto.response.CursorSlice;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.entity.QUser;
import com.ootd.fitme.domain.user.enums.SortDirection;
import com.ootd.fitme.domain.user.enums.UserSortBy;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
    private static final QUser user = QUser.user;
    private static final QProfile profile = QProfile.profile;

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorSlice<UserDto> findUsersByCondition(UserSearchCondition condition) {
        UserSortBy sortBy = condition.sortBy() == null ? UserSortBy.CREATED_AT : condition.sortBy();
        SortDirection sortDirection = condition.sortDirection() == null
                ? SortDirection.DESCENDING : condition.sortDirection();

        BooleanBuilder where = baseFilter(condition);
        BooleanExpression cursorCondition = null;

        if (condition.cursor() != null && condition.idAfter() != null) {
            UUID idAfter = condition.idAfter();

            if (sortBy == UserSortBy.CREATED_AT) {
                Instant cursor = Instant.parse(condition.cursor());
                if (sortDirection == SortDirection.DESCENDING) {
                    cursorCondition = user.createdAt.lt(cursor)
                            .or(user.createdAt.eq(cursor).and(user.id.gt(idAfter)));
                } else {
                    cursorCondition = user.createdAt.gt(cursor)
                            .or(user.createdAt.eq(cursor).and(user.id.gt(idAfter)));
                }
            } else {
                String cursor = condition.cursor();
                if (sortDirection == SortDirection.DESCENDING) {
                    cursorCondition = user.email.lt(cursor)
                            .or(user.email.eq(cursor).and(user.id.gt(idAfter)));
                } else {
                    cursorCondition = user.email.gt(cursor)
                            .or(user.email.eq(cursor).and(user.id.gt(idAfter)));
                }
            }

            if (cursorCondition != null) {
                where.and(cursorCondition);
            }
        }

        OrderSpecifier<?> primaryOrder = switch (sortBy) {
            case CREATED_AT -> sortDirection == SortDirection.ASCENDING
                    ? user.createdAt.asc()
                    : user.createdAt.desc();
            case EMAIL -> sortDirection == SortDirection.ASCENDING
                    ? user.email.asc()
                    : user.email.desc();
        };

        int limit = condition.limit();

        List<UserDto> fetched = queryFactory
                .select(Projections.constructor(
                        UserDto.class,
                        user.id,
                        user.createdAt,
                        user.email,
                        profile.name,
                        user.role,
                        user.locked
                ))
                .from(user)
                .join(profile).on(profile.user.id.eq(user.id))
                .where(where)
                .orderBy(primaryOrder, user.id.asc())
                .limit(limit + 1L)
                .fetch();

        boolean hasNext = fetched.size() > limit;
        List<UserDto> data = hasNext ? fetched.subList(0, limit) : fetched;

        String nextCursor = null;
        UUID nextIdAfter = null;
        if (hasNext && !data.isEmpty()) {
            UserDto last = data.get(data.size() - 1);
            nextCursor = sortBy.extractCursor(last);
            nextIdAfter = last.id();
        }

        Long totalCount = queryFactory
                .select(user.count())
                .from(user)
                .join(profile).on(profile.user.id.eq(user.id))
                .where(baseFilter(condition))
                .fetchOne();

        return new CursorSlice<>(
                data,
                nextCursor,
                nextIdAfter,
                hasNext,
                totalCount == null ? 0L : totalCount
        );
    }

    private BooleanBuilder baseFilter(UserSearchCondition condition) {
        BooleanBuilder where = new BooleanBuilder();

        if (condition.emailLike() != null && !condition.emailLike().isBlank()) {
            where.and(user.email.containsIgnoreCase(condition.emailLike().trim()));
        }

        if (condition.roleEqual() != null) {
            where.and(user.role.eq(condition.roleEqual()));
        }

        if (condition.locked() != null) {
            where.and(user.locked.eq(condition.locked()));
        }

        return where;
    }
}
