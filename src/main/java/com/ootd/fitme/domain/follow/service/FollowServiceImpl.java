package com.ootd.fitme.domain.follow.service;

import com.ootd.fitme.domain.follow.dto.request.FollowCreateRequest;
import com.ootd.fitme.domain.follow.dto.response.FollowCursorDto;
import com.ootd.fitme.domain.follow.dto.response.FollowDto;
import com.ootd.fitme.domain.follow.dto.response.FollowListResponse;
import com.ootd.fitme.domain.follow.dto.response.FollowSummaryDto;
import com.ootd.fitme.domain.follow.entity.Follow;
import com.ootd.fitme.domain.follow.enums.SortBy;
import com.ootd.fitme.domain.follow.enums.SortDirection;
import com.ootd.fitme.domain.follow.mapper.FollowMapper;
import com.ootd.fitme.domain.follow.repository.FollowRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final FollowCountService followCountService;

    @Override
    @Transactional
    public FollowDto createFollow(FollowCreateRequest request) {
        if(followRepository.findByFollowerIdAndFolloweeId(
                request.followerId(), request.followeeId()).isPresent()) {
            throw new IllegalArgumentException("이미 팔로우한 사용자입니다.");
        }

        Follow follow = Follow.create(request.followerId(), request.followeeId());
        Follow savedFollow = followRepository.save(follow);
        followCountService.increaseFollowCount(savedFollow);

        return FollowMapper.toDto(savedFollow);
    }

    @Override
    public FollowListResponse getFollowers(
            UUID followeeId, String cursor, UUID idAfter, int limit, String nameLike) {

        List<FollowCursorDto> followers = followRepository.findFollowers(
                followeeId, cursor, idAfter, limit, nameLike);

        return followList(followers, limit);
    }

    @Override
    public FollowListResponse getFollowings(
            UUID followerId, String cursor, UUID idAfter, int limit, String nameLike) {

        List<FollowCursorDto> followings = followRepository.findFollowings(
                followerId, cursor, idAfter, limit, nameLike);

        return followList(followings, limit);
    }

    @Override
    public FollowSummaryDto getFollowSummary(UUID userId) {
        // TODO : 팔로워 요약 조회 로직
        return null;
    }

    @Override
    @Transactional
    public void cancelFollow(UUID followId) {
        Follow follow = followRepository.findById(followId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팔로우입니다."));
        followCountService.decreaseFollowCount(follow);
        followRepository.deleteById(followId);
    }

    private FollowListResponse followList(List<FollowCursorDto> result, int limit) {
        boolean hasNext = result.size() > limit;
        List<FollowCursorDto> followCursorDto = hasNext ? result.subList(0, limit) : result;

        String nextCursor = null;
        UUID nextIdAfter = null;
        if (hasNext) {
            FollowCursorDto lastItem = followCursorDto.get(followCursorDto.size() - 1);
            nextCursor = lastItem.createdAt().toString();
            nextIdAfter = lastItem.id();
        }

        List<FollowDto> data = followCursorDto.stream()
                .map(FollowMapper::toDto)
                .toList();

        // TODO : totalcount (count 기능) 구현하기
        return new FollowListResponse(
                data, nextCursor, nextIdAfter, hasNext, 0L, SortBy.createdAt, SortDirection.DESCENDING);
    }
}
