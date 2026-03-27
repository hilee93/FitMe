package com.ootd.fitme.domain.follow.service;

import com.ootd.fitme.domain.follow.dto.request.FollowCreateRequest;
import com.ootd.fitme.domain.follow.dto.response.FollowDto;
import com.ootd.fitme.domain.follow.dto.response.FollowListResponse;
import com.ootd.fitme.domain.follow.dto.response.FollowSummaryDto;

import java.util.UUID;

public interface FollowService {

    FollowDto createFollow(FollowCreateRequest request);
    FollowListResponse getFollowers(UUID followeeId, String cursor, UUID idAfter, Integer limit, String nameLike);
    FollowListResponse getFollowings(UUID followerId, String cursor, UUID idAfter, Integer limit, String nameLike);
    FollowSummaryDto getFollowSummary(UUID userId, UUID myId);
    void cancelFollow(UUID followId);
}
