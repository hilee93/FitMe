package com.ootd.fitme.domain.feed.controller.docs;

import com.ootd.fitme.domain.feed.dto.request.FeedCommentCreateRequest;
import com.ootd.fitme.domain.feed.dto.request.FeedCommentSearchCondition;
import com.ootd.fitme.domain.comment.dto.response.CommentCursorResponseDto;
import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.feed.dto.request.*;
import com.ootd.fitme.domain.feed.dto.response.FeedCursorResponseDto;
import com.ootd.fitme.domain.feed.dto.response.FeedResponseDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Feed API", description = "피드 관리 관련 API")
public interface FeedControllerDocs {

    ResponseEntity<FeedCursorResponseDto> getAllFeeds(FeedSearchCondition feedSearchCondition);

    ResponseEntity<FeedResponseDto> createFeed(@Valid FeedCreateRequest feedCreateRequest);

    ResponseEntity<FeedResponseDto> updateFeed(UUID feedId, @Valid FeedUpdateRequestDto feedUpdateRequestDto);

    ResponseEntity<Void> deleteFeed(UUID feedId);

    ResponseEntity<Void> addLike(UUID feedId);

    ResponseEntity<Void> removeLike(UUID feedId);

    ResponseEntity<CommentResponseDto> addComment(FeedCommentCreateRequest feedCommentCreateRequest);

    ResponseEntity<CommentCursorResponseDto> getAllFeedComments(FeedCommentSearchCondition feedCommentSearchCondition);

}
