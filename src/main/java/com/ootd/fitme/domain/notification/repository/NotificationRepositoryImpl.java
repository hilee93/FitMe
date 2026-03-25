package com.ootd.fitme.domain.notification.repository;

import com.ootd.fitme.domain.notification.dto.request.NotificationPageRequest;
import com.ootd.fitme.domain.notification.entity.Notification;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.ootd.fitme.domain.notification.entity.QNotification.notification;

public class NotificationRepositoryImpl implements NotificationRepositoryCustom {


    private final JPAQueryFactory queryFactory;

    public NotificationRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Slice<Notification> search(NotificationPageRequest request) {

        List<Notification> content = queryFactory
                .selectFrom(notification)
                .where(
                        notification.user.id.eq(request.userId()),
                        Cursor(request)
                )
                .orderBy(notification.createdAt.asc(),notification.id.asc())
                .limit(request.limit() + 1)
                .fetch();

        return  Slice(content, request.limit());
    }

    private BooleanExpression Cursor(NotificationPageRequest request) {

        // 첫 페이지: 커서가 없으면 조건 없음
        if (request.cursor() == null || request.idAfter() == null) {
            return null;
        }

        Instant cursorTime = Instant.parse(request.cursor());
        UUID idAfter = UUID.fromString(request.idAfter());

        return notification.createdAt.gt(cursorTime)
                .or(
                        notification.createdAt.eq(cursorTime)
                                .and(notification.id.gt(idAfter))
                );
    }

    private <T> Slice<T> Slice(List<T> content, int limit) {

        boolean hasNext = content.size() > limit;

        if (hasNext) {
            content.remove(limit);
        }

        return new SliceImpl<>(content, Pageable.unpaged(), hasNext);
    }

}
