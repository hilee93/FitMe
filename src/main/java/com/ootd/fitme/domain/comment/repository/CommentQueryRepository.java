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
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
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
        // TODO: 쿼리문 작성
        return null;
    }


}
