package com.ootd.fitme.domain.comment.service;

import com.ootd.fitme.domain.comment.dto.request.CommentSearchCondition;
import com.ootd.fitme.domain.comment.dto.response.CommentCursorResponseDto;
import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.comment.repository.CommentQueryRepository;
import com.ootd.fitme.domain.feed.dto.response.CursorResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentQueryRepository commentQueryRepository;

    public CommentResponseDto getFeedComment(UUID commentId) {
        return commentQueryRepository.findFeedCommentById(commentId);
    }

    public CommentCursorResponseDto getComments(CommentSearchCondition condition) {
        CursorResult<CommentResponseDto> commentCursorResult = commentQueryRepository.findCommentsByFeedId(condition);
        return CommentCursorResponseDto.from(commentCursorResult);
    }
}
