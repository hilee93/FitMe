package com.ootd.fitme.domain.feed.controller;

import com.ootd.fitme.domain.comment.dto.request.CommentSearchCondition;
import com.ootd.fitme.domain.comment.dto.response.CommentCursorResponseDto;
import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.comment.service.CommentService;
import com.ootd.fitme.domain.feed.controller.docs.FeedControllerDocs;
import com.ootd.fitme.domain.feed.dto.request.*;
import com.ootd.fitme.domain.feed.dto.response.FeedCursorResponseDto;
import com.ootd.fitme.domain.feed.dto.response.FeedResponseDto;
import com.ootd.fitme.domain.feed.service.FeedService;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feeds")
public class FeedController implements FeedControllerDocs {

    private final FeedService feedService;
    private final CommentService commentService;

    @Override
    @GetMapping
    public ResponseEntity<FeedCursorResponseDto> getAllFeeds(FeedSearchCondition feedSearchCondition) {
        return null;
    }

    @Override
    @PostMapping
    public ResponseEntity<FeedResponseDto> createFeed(@RequestBody @Valid FeedCreateRequest feedCreateRequest) {
        FeedResponseDto responseDto = feedService.createFeed(feedCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Override
    @PatchMapping("/{feedId}")
    public ResponseEntity<FeedResponseDto> updateFeed(
            @PathVariable UUID feedId,
            @RequestBody @Valid FeedUpdateRequestDto feedUpdateRequestDto,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        FeedResponseDto feedResponseDto = feedService.updateFeed(feedId, userPrincipal.getUserId(), feedUpdateRequestDto);
        return ResponseEntity.ok(feedResponseDto);
    }

    @Override
    @DeleteMapping("/{feedId}")
    public ResponseEntity<Void> deleteFeed(@PathVariable UUID feedId) {
        feedService.deleteFeed(feedId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/{feedId}/like")
    public ResponseEntity<Void> addLike(@PathVariable UUID feedId, @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        feedService.likeFeed(feedId, userPrincipal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{feedId}/like")
    public ResponseEntity<Void> removeLike(@PathVariable UUID feedId, @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        feedService.unlikeFeed(feedId, userPrincipal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/{feedId}/comments")
    public ResponseEntity<CommentResponseDto> addComment(
            @RequestBody @Valid FeedCommentCreateRequest feedCommentCreateRequest,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        CommentResponseDto feedComment = commentService.createFeedComment(feedCommentCreateRequest, userPrincipal.getUserId());
        return ResponseEntity.ok(feedComment);
    }

    @Override
    @GetMapping("/{feedId}/comments")
    public ResponseEntity<CommentCursorResponseDto> getAllFeedComments(
            @Valid CommentSearchCondition commentSearchCondition,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal
    ) {
        commentService.getFeedComments(commentSearchCondition);
        return null;
    }
}
