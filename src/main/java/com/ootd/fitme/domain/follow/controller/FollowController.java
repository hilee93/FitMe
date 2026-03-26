package com.ootd.fitme.domain.follow.controller;

import com.ootd.fitme.domain.follow.controller.docs.FollowControllerDocs;
import com.ootd.fitme.domain.follow.dto.request.FollowCreateRequest;
import com.ootd.fitme.domain.follow.dto.response.FollowDto;
import com.ootd.fitme.domain.follow.dto.response.FollowListResponse;
import com.ootd.fitme.domain.follow.dto.response.FollowSummaryDto;
import com.ootd.fitme.domain.follow.service.FollowService;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follows")
public class FollowController implements FollowControllerDocs {

    private final FollowService followService;

    @Override
    @PostMapping
    public ResponseEntity<FollowDto> createFollow(@Valid @RequestBody FollowCreateRequest request) {
        // TODO: followerId는 인증된 사용자 ID로 교체 예정 (@AuthenticationPrincipal)
        FollowDto follow = followService.createFollow(request);
        return ResponseEntity.status(201).body(follow);
    }

    @Override
    @GetMapping("/summary")
    public ResponseEntity<FollowSummaryDto> getFollowSummary(
            @RequestParam UUID userId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        UUID myId = principal.getUserId();

        FollowSummaryDto result = followService.getFollowSummary(userId, myId);
        return ResponseEntity.status(200).body(result);
    }

    @Override
    @GetMapping("/followings")
    public ResponseEntity<FollowListResponse> getFollowings(
            @RequestParam UUID followerId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam int limit,
            @RequestParam(required = false) String nameLike) {
        FollowListResponse followings = followService.getFollowings(followerId, cursor, idAfter, limit, nameLike);
        return ResponseEntity.status(200).body(followings);
    }

    @Override
    @GetMapping("/followers")
    public ResponseEntity<FollowListResponse> getFollowers(
            @RequestParam UUID followeeId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam int limit,
            @RequestParam(required = false) String nameLike) {
        FollowListResponse followers = followService.getFollowers(followeeId, cursor, idAfter, limit, nameLike);
        return ResponseEntity.status(200).body(followers);
    }

    @Override
    @DeleteMapping("/{followId}")
    public ResponseEntity<Void> cancelFollow(@PathVariable UUID followId) {
        followService.cancelFollow(followId);
        return ResponseEntity.noContent().build();
    }
}
