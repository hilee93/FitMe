package com.ootd.fitme.domain.follow.controller.docs;

import com.ootd.fitme.domain.follow.dto.request.FollowCreateRequest;
import com.ootd.fitme.domain.follow.dto.request.FollowSearchCondition;
import com.ootd.fitme.domain.follow.dto.response.FollowDto;
import com.ootd.fitme.domain.follow.dto.response.FollowListResponse;
import com.ootd.fitme.domain.follow.dto.response.FollowSummaryDto;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Follow API", description = "팔로우 관련 API")
public interface FollowControllerDocs {

    @Operation(summary = "팔로우 생성", description = "팔로우 생성 API")
    @ApiResponse(responseCode = "201", description = "팔로우 생성 성공")
    @ApiResponse(responseCode = "400", description = "팔로우 생성 실패")
    ResponseEntity<FollowDto> createFollow(FollowCreateRequest request);

    @Operation(summary = "팔로우 요약 조회", description = "팔로우 요약 조회 API")
    @ApiResponse(responseCode = "200", description = "팔로우 요약 조회 성공")
    @ApiResponse(responseCode = "400", description = "팔로우 요약 조회 실패")
    ResponseEntity<FollowSummaryDto> getFollowSummary(
            @Parameter(description = "조회할 사용자의 UUID", required = true) UUID userId,
            CustomUserPrincipal principal);

    @Operation(summary = "팔로잉 목록 조회", description = "팔로잉 목록 조회 API")
    @ApiResponse(responseCode = "200", description = "팔로잉 목록 조회 성공")
    @ApiResponse(responseCode = "400", description = "팔로잉 목록 조회 실패")
    ResponseEntity<FollowListResponse> getFollowings(
            @Parameter(description = "팔로잉 목록을 조회할 사용자의 UUID", required = true) UUID followerId,
            FollowSearchCondition condition);

    @Operation(summary = "팔로워 목록 조회", description = "팔로워 목록 조회 API")
    @ApiResponse(responseCode = "200", description = "팔로워 목록 조회 성공")
    @ApiResponse(responseCode = "400", description = "팔로워 목록 조회 실패")
    ResponseEntity<FollowListResponse> getFollowers(
            @Parameter(description = "팔로워 목록을 조회할 사용자의 UUID", required = true) UUID followeeId,
            FollowSearchCondition condition);

    @Operation(summary = "팔로우 취소", description = "팔로우 취소 API")
    @ApiResponse(responseCode = "204", description = "팔로우 취소 성공")
    @ApiResponse(responseCode = "400", description = "팔로우 취소 실패")
    ResponseEntity<Void> cancelFollow(
            @Parameter(description = "취소할 팔로우의 UUID", required = true) UUID followId);
}
