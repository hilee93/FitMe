package com.ootd.fitme.domain.follow.service;

import com.ootd.fitme.domain.follow.dto.request.FollowCreateRequest;
import com.ootd.fitme.domain.follow.dto.response.FollowDto;
import com.ootd.fitme.domain.follow.dto.response.FollowListResponse;
import com.ootd.fitme.domain.follow.dto.response.FollowSummaryDto;
import com.ootd.fitme.domain.follow.repository.FollowRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;

    @Override
    public FollowDto createFollow(FollowCreateRequest request) {
        // TODO : 팔로우 생성로직
        return null;
    }

    @Override
    public FollowListResponse getFollowers(
            UUID followeeId, String Cursor, UUID idAfter, int limit, String nameLike) {
        // TODO : 팔로워 조회 로직
        return null;
    }

    @Override
    public FollowListResponse getFollowings(
            UUID followerId, String Cursor, UUID idAfter, int limit, String nameLike) {
        // TODO : 팔로잉 조회 로직
        return null;
    }

    @Override
    public FollowSummaryDto getFollowSummary(UUID userId) {
        // TODO : 팔로워 요약 조회 로직
        return null;
    }

    @Override
    public void cancelFollow(UUID followId) {
        // TODO : 팔로우 취소 로직
    }
}
