package com.ootd.fitme.domain.feed.controller;

import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.feed.controller.docs.FeedControllerDocs;
import com.ootd.fitme.domain.feed.controller.docs.FeedCreateRequest;
import com.ootd.fitme.domain.feed.dto.request.FeedSearchCondition;
import com.ootd.fitme.domain.feed.dto.request.FeedUpdateRequestDto;
import com.ootd.fitme.domain.feed.dto.response.FeedCursorResponseDto;
import com.ootd.fitme.domain.feed.dto.response.FeedResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feeds")
public class FeedController implements FeedControllerDocs {
    @Override
    @GetMapping
    public ResponseEntity<FeedCursorResponseDto> getAllFeeds(FeedSearchCondition feedSearchCondition) {
        return null;
    }

    @Override
    public ResponseEntity<FeedResponseDto> createFeed(FeedCreateRequest feedCreateRequest) {
        return null;
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
    public ResponseEntity<Void> deleteFeed(UUID feedId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> addLike(UUID feedId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> removeLike(UUID feedId) {
        return null;
    }

    @Override
    public ResponseEntity<CommentResponseDto> addComment(UUID feedId, String comment) {
        return null;
    }
}
