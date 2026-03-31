package com.ootd.fitme.domain.feed.controller.docs;

import com.ootd.fitme.domain.comment.dto.request.CommentSearchCondition;
import com.ootd.fitme.domain.comment.dto.response.CommentCursorResponseDto;
import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.feed.dto.request.*;
import com.ootd.fitme.domain.feed.dto.response.FeedCursorResponseDto;
import com.ootd.fitme.domain.feed.dto.response.FeedResponseDto;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Feed API", description = "피드 관리 관련 API")
public interface FeedControllerDocs {

    ResponseEntity<FeedCursorResponseDto> getAllFeeds(FeedSearchCondition feedSearchCondition);

    ResponseEntity<FeedResponseDto> createFeed(FeedCreateRequest feedCreateRequest);

    ResponseEntity<FeedResponseDto> updateFeed(UUID feedId, FeedUpdateRequestDto feedUpdateRequestDto, @Parameter(hidden = true) CustomUserPrincipal userPrincipal);

    ResponseEntity<Void> deleteFeed(UUID feedId);

    ResponseEntity<Void> addLike(UUID feedId, @Parameter(hidden = true) CustomUserPrincipal userPrincipal);

    ResponseEntity<Void> removeLike(UUID feedId, @Parameter(hidden = true) CustomUserPrincipal userPrincipal);

    ResponseEntity<CommentResponseDto> addComment(FeedCommentCreateRequest feedCommentCreateRequest, @Parameter(hidden = true) CustomUserPrincipal userPrincipal);

    ResponseEntity<CommentCursorResponseDto> getAllFeedComments(CommentSearchCondition feedCommentSearchCondition, @Parameter(hidden = true) CustomUserPrincipal userPrincipal);

}
