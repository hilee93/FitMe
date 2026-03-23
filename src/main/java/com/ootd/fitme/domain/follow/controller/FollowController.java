package com.ootd.fitme.domain.follow.controller;

import com.ootd.fitme.domain.follow.controller.docs.FollowControllerDocs;
import com.ootd.fitme.domain.follow.dto.request.FollowCreateRequest;
import com.ootd.fitme.domain.follow.dto.response.FollowDto;
import com.ootd.fitme.domain.follow.dto.response.FollowListResponse;
import com.ootd.fitme.domain.follow.dto.response.FollowSummaryDto;
import com.ootd.fitme.domain.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follows")
public class FollowController implements FollowControllerDocs {

    private final FollowService followService;

    @Override
    public ResponseEntity<FollowDto> createFollow(FollowCreateRequest followCreateRequest) {
        return null;
    }

    @Override
    public ResponseEntity<FollowSummaryDto> getFollowSummary(UUID userId) {
        return null;
    }

    //TODO : 커서 페이지네이션
    @Override
    public ResponseEntity<FollowListResponse> getFollowings(
            UUID followerId, String cursor, UUID idAfter, int limit, String nameLike) {
        return null;
    }

    //TODO : 커서 페이지네이션
    @Override
    public ResponseEntity<FollowListResponse> getFollowers(
            UUID followeeId, String cursor, UUID idAfter, int limit, String nameLike) {
        return null;
    }

    @Override
    public ResponseEntity<Void> cancelFollow(UUID followId) {
        return null;
    }
}
