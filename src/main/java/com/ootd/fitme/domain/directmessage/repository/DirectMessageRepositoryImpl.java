package com.ootd.fitme.domain.directmessage.repository;

import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDto;
import com.ootd.fitme.domain.directmessage.dto.response.UserSummary;
import com.ootd.fitme.domain.directmessage.entity.QDirectMessage;
import com.ootd.fitme.domain.profile.entity.QProfile;
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
public class DirectMessageRepositoryImpl implements DirectMessageRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<DirectMessageDto> findDirectMessages(UUID myId, UUID targetId, String cursor, UUID idAfter, int limit) {

        QDirectMessage directMessage = QDirectMessage.directMessage;
        QProfile senderProfile = new QProfile("senderProfile");
        QProfile receiverProfile = new QProfile("receiverProfile");

        return jpaQueryFactory
                .select(
                        Projections.constructor(DirectMessageDto.class,
                                directMessage.id,
                                directMessage.createdAt,
                                Projections.constructor(UserSummary.class,
                                        directMessage.senderId,
                                        senderProfile.name,
                                        senderProfile.profileImageUrl
                                        ),
                                Projections.constructor(UserSummary.class,
                                        directMessage.receiverId,
                                        receiverProfile.name,
                                        receiverProfile.profileImageUrl
                                        ),
                                directMessage.content
                                )
                )
                .from(directMessage)
                .join(senderProfile).on(senderProfile.user.id.eq(directMessage.senderId))
                .join(receiverProfile).on(receiverProfile.user.id.eq(directMessage.receiverId))
                .where(
                        directMessage.senderId.eq(myId).and(directMessage.receiverId.eq(targetId))
                                .or(directMessage.senderId.eq(targetId).and(directMessage.receiverId.eq(myId))),
                        cursorCondition(cursor, idAfter)
                )
                .orderBy(directMessage.createdAt.desc(), directMessage.id.asc())
                .limit(limit + 1)
                .fetch();
    }
    private BooleanExpression cursorCondition(String cursor, UUID idAfter) {
        if (cursor == null || idAfter == null) return null;

        Instant nextCursor = Instant.parse(cursor);
        QDirectMessage directMessage = QDirectMessage.directMessage;
        BooleanExpression condition = directMessage.createdAt.lt(nextCursor);
        return condition.or(directMessage.createdAt.eq(nextCursor).and(directMessage.id.gt(idAfter)));

    }
}
