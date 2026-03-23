package com.ootd.fitme.domain.follow.service;

import com.ootd.fitme.domain.follow.dto.request.FollowCreateRequest;
import com.ootd.fitme.domain.follow.dto.response.FollowDto;
import com.ootd.fitme.domain.follow.dto.response.FollowListResponse;
import com.ootd.fitme.domain.follow.dto.response.FollowSummaryDto;
import com.ootd.fitme.domain.follow.entity.Follow;
import com.ootd.fitme.domain.follow.mapper.FollowMapper;
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
    @Transactional
    public FollowDto createFollow(FollowCreateRequest request) {

        if(request.followerId().equals(request.followeeId())) {
            throw new IllegalArgumentException("자신을 팔로우 할 수 없습니다.");
        }
        if(followRepository.findByFollowerIdAndFolloweeId(
                request.followerId(), request.followeeId()).isPresent()) {
            throw new IllegalArgumentException("이미 팔로우한 사용자입니다.");
        }

        Follow follow = Follow.create(request.followerId(), request.followeeId());
        Follow savedFollow = followRepository.save(follow);

        return FollowMapper.toDto(savedFollow);
    }

    @Override
    public FollowListResponse getFollowers(
            UUID followeeId, String cursor, UUID idAfter, int limit, String nameLike) {
        // TODO : 팔로워 조회 로직
        return null;
    }

    @Override
    public FollowListResponse getFollowings(
            UUID followerId, String cursor, UUID idAfter, int limit, String nameLike) {
        // TODO : 팔로잉 조회 로직
        return null;
    }

    @Override
    public FollowSummaryDto getFollowSummary(UUID userId) {
        // TODO : 팔로워 요약 조회 로직
        return null;
    }

    @Override
    @Transactional
    public void cancelFollow(UUID followId) {
        followRepository.findById(followId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팔로우입니다."));
        followRepository.deleteById(followId);
    }
}
