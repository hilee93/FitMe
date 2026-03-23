package com.ootd.fitme.domain.feed.controller;

import com.ootd.fitme.domain.feed.dto.request.FeedCommentCreateRequest;
import com.ootd.fitme.domain.feed.dto.request.FeedCommentSearchCondition;
import com.ootd.fitme.domain.comment.dto.response.CommentCursorResponseDto;
import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.feed.controller.docs.FeedControllerDocs;
import com.ootd.fitme.domain.feed.dto.request.*;
import com.ootd.fitme.domain.feed.dto.response.FeedCursorResponseDto;
import com.ootd.fitme.domain.feed.dto.response.FeedResponseDto;
import com.ootd.fitme.domain.feed.service.FeedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feeds")
public class FeedController implements FeedControllerDocs {

    private final FeedService feedService;

    @Override
    @GetMapping
    public ResponseEntity<FeedCursorResponseDto> getAllFeeds(FeedSearchCondition feedSearchCondition) {
        return null;
    }

    @Override
    @PostMapping
    public ResponseEntity<FeedResponseDto> createFeed(@RequestBody @Valid FeedCreateRequest feedCreateRequest) {
        FeedResponseDto responseDto = feedService.createFeed(feedCreateRequest);
        return ResponseEntity.ok(responseDto);
    }

    @Override
    @PatchMapping("/{feedId}")
    public ResponseEntity<FeedResponseDto> updateFeed(
            @PathVariable UUID feedId,
            @RequestBody @Valid FeedUpdateRequestDto feedUpdateRequestDto // TODO: MethodArgumentNotValidException 추가 처리
    ) {
        return null;
    }

    @Override
    @DeleteMapping("/{feedId}")
    public ResponseEntity<Void> deleteFeed(@PathVariable UUID feedId) {
        return null;
    }

    @Override
    @PostMapping("/{feedId}/like")
    public ResponseEntity<Void> addLike(@PathVariable UUID feedId) {
        return null;
    }

    @Override
    @DeleteMapping("/{feedId}/like")
    public ResponseEntity<Void> removeLike(@PathVariable UUID feedId) {
        return null;
    }

    @Override
    @PostMapping("/{feedId}/comments")
    public ResponseEntity<CommentResponseDto> addComment(@RequestBody @Valid FeedCommentCreateRequest feedCommentCreateRequest) {
        return null;
    }

    @Override
    @GetMapping("/{feedId}/comments")
    public ResponseEntity<CommentCursorResponseDto> getAllFeedComments(@Valid FeedCommentSearchCondition feedCommentSearchCondition) {
        return null;
    }
}
