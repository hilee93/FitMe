package com.ootd.fitme.domain.comment.repository;

import com.ootd.fitme.domain.comment.dto.request.CommentSearchCondition;
import com.ootd.fitme.domain.comment.dto.response.CommentFlatRow;
import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.comment.entity.QComment;
import com.ootd.fitme.domain.comment.exception.CommentNotFoundException;
import com.ootd.fitme.domain.feed.dto.response.CursorResult;
import com.ootd.fitme.domain.profile.entity.QProfile;
import com.ootd.fitme.domain.user.entity.QUser;
import com.ootd.fitme.global.exception.ErrorCode;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CommentQueryRepository {

    private final JPAQueryFactory queryFactory;


    public CommentResponseDto findFeedCommentById(UUID commentId) {

        QComment comment = QComment.comment;
        QProfile profile = QProfile.profile;
        QUser user = QUser.user;


        CommentFlatRow result = queryFactory.select(
                        Projections.constructor(
                                CommentFlatRow.class,
                                comment.id,
                                comment.createdAt,
                                comment.feed.id,
                                comment.user.id,
                                profile.name,
                                profile.profileImageUrl,
                                comment.content
                        )
                )
                .from(comment)
                .join(comment.user, user)
                .join(profile).on(profile.user.eq(user))
                .where(comment.id.eq(commentId))
                .fetchOne();

        if (result == null) {
            throw new CommentNotFoundException(ErrorCode.COMMENT_NOT_FOUND);
        }
        return CommentResponseDto.from(result);

    }

    public CursorResult<CommentResponseDto> findCommentsByFeedId(CommentSearchCondition condition) {
        QComment comment = QComment.comment;
        QProfile profile = QProfile.profile;
        QUser user = QUser.user;


        int size = condition.limit();
        log.debug("size: {}", size);

        List<CommentFlatRow> commentFlatRows = queryFactory.select(
                        Projections.constructor(
                                CommentFlatRow.class,
                                comment.id,
                                comment.createdAt,
                                comment.feed.id,
                                comment.user.id,
                                profile.name,
                                profile.profileImageUrl,
                                comment.content
                        )
                )
                .from(comment)
                .join(comment.user, user)
                .join(profile).on(profile.user.eq(user))
                .where(
                        comment.feed.id.eq(condition.feedId()),
                        cursorCondition(comment, condition.cursor(), condition.idAfter())
                )
                .orderBy(comment.createdAt.desc(), comment.id.asc())
                .limit(size + 1)
                .fetch();

        boolean hasNext = commentFlatRows.size() > size;
        if (hasNext) {
            commentFlatRows.remove(size);
        }

        long total = countCommentsByFeedId(condition.feedId());

        List<CommentResponseDto> commentResponseDtos = commentFlatRows.stream()
                .map(CommentResponseDto::from)
                .toList();
        return new CursorResult<>(
                commentResponseDtos,
                hasNext,
                total
        );
    }

    private long countCommentsByFeedId(UUID feedId) {
        QComment comment = QComment.comment;
        Long total = queryFactory.select(comment.count())
                .from(comment)
                .where(comment.feed.id.eq(feedId))
                .fetchOne();

        return total == null ? 0 : total;
    }

    private BooleanExpression cursorCondition(QComment comment, String cursor, UUID idAfter) {
        if (cursor == null || idAfter == null) return null;

        Instant nextCursor = Instant.parse(cursor);
        BooleanExpression condition = comment.createdAt.lt(nextCursor);
        return condition.or(comment.createdAt.eq(nextCursor).and(comment.id.gt(idAfter)));
    }


}
