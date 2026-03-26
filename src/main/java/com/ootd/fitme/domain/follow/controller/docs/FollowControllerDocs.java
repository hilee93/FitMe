package com.ootd.fitme.domain.follow.controller.docs;

import com.ootd.fitme.domain.follow.dto.request.FollowCreateRequest;
import com.ootd.fitme.domain.follow.dto.response.FollowDto;
import com.ootd.fitme.domain.follow.dto.response.FollowListResponse;
import com.ootd.fitme.domain.follow.dto.response.FollowSummaryDto;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Follow API", description = "팔로우 관련 API")
public interface FollowControllerDocs {

    ResponseEntity<FollowDto> createFollow(FollowCreateRequest request);

    ResponseEntity<FollowSummaryDto> getFollowSummary(UUID userId, CustomUserPrincipal principal);

    ResponseEntity<FollowListResponse> getFollowings(
            UUID followerId, String cursor, UUID idAfter, int limit, String nameLike);

    ResponseEntity<FollowListResponse> getFollowers(
            UUID followeeId, String cursor, UUID idAfter, int limit, String nameLike);

    ResponseEntity<Void> cancelFollow(UUID followId);


}
