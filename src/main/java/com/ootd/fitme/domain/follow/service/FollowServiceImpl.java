package com.ootd.fitme.domain.follow.service;

import com.ootd.fitme.domain.follow.dto.request.FollowCreateRequest;
import com.ootd.fitme.domain.follow.dto.response.*;
import com.ootd.fitme.domain.follow.entity.Follow;
import com.ootd.fitme.domain.follow.enums.SortBy;
import com.ootd.fitme.domain.follow.enums.SortDirection;
import com.ootd.fitme.domain.follow.event.FollowCreateEvent;
import com.ootd.fitme.domain.follow.exception.FollowAlreadyExistsException;
import com.ootd.fitme.domain.follow.exception.FollowNotFoundException;
import com.ootd.fitme.domain.follow.mapper.FollowMapper;
import com.ootd.fitme.domain.follow.repository.FollowProfileQueryRepository;
import com.ootd.fitme.domain.follow.repository.FollowRepository;

import com.ootd.fitme.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final FollowCountService followCountService;
    private final FollowProfileQueryRepository followProfileQueryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public FollowDto createFollow(FollowCreateRequest request) {
        if(followRepository.findByFollowerIdAndFolloweeId(
                request.followerId(), request.followeeId()).isPresent()) {
            throw new FollowAlreadyExistsException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        Follow follow = Follow.create(request.followerId(), request.followeeId());
        Follow savedFollow = followRepository.save(follow);
        followCountService.increaseFollowCount(savedFollow);

        UserSummary follower = followProfileQueryRepository.findUserSummaryByUserId(request.followerId());
        UserSummary followee = followProfileQueryRepository.findUserSummaryByUserId(request.followeeId());

        eventPublisher.publishEvent(new FollowCreateEvent(
                savedFollow.getId(),
                savedFollow.getFolloweeId(),
                savedFollow.getFollowerId(),
                follower.name(),
                savedFollow.getCreatedAt()
        ));

        return FollowMapper.toDto(savedFollow, followee, follower);
    }

    @Override
    public FollowListResponse getFollowers(
            UUID followeeId, String cursor, UUID idAfter, Integer limit, String nameLike) {

        List<FollowCursorDto> followers = followRepository.findFollowers(
                followeeId, cursor, idAfter, limit, nameLike);

        long totalCount = (nameLike == null || nameLike.isBlank())
                ? followProfileQueryRepository.findFollowerCountByUserId(followeeId)
                : followRepository.countFollowers(followeeId, nameLike);

        return followList(followers, limit, totalCount);
    }

    @Override
    public FollowListResponse getFollowings(
            UUID followerId, String cursor, UUID idAfter, Integer limit, String nameLike) {

        List<FollowCursorDto> followings = followRepository.findFollowings(
                followerId, cursor, idAfter, limit, nameLike);

        long totalCount = (nameLike == null || nameLike.isBlank())
                ? followProfileQueryRepository.findFolloweeCountByUserId(followerId)
                : followRepository.countFollowings(followerId, nameLike);

        return followList(followings, limit, totalCount);
    }

    @Override
    public FollowSummaryDto getFollowSummary(UUID userId, UUID myId) {

        int followerCount = followProfileQueryRepository.findFollowerCountByUserId(userId);
        int followeeCount = followProfileQueryRepository.findFolloweeCountByUserId(userId);

        Optional<Follow> followedByMe = followRepository.findByFollowerIdAndFolloweeId(myId, userId);
        boolean isFollowedByMe = followedByMe.isPresent();

        UUID followedByMeId = followedByMe.map(f -> f.getId())
                .orElse(null);

        Optional<Follow> followingMe = followRepository.findByFollowerIdAndFolloweeId(userId, myId);
        boolean isFollowingMe = followingMe.isPresent();

        return new FollowSummaryDto(
                userId,
                followerCount,
                followeeCount,
                isFollowedByMe,
                followedByMeId,
                isFollowingMe
        );
    }

    @Override
    @Transactional
    public void cancelFollow(UUID followId) {
        Follow follow = followRepository.findById(followId)
                .orElseThrow(() -> new FollowNotFoundException(ErrorCode.FOLLOW_NOT_FOUND));
        followCountService.decreaseFollowCount(follow);
        followRepository.deleteById(followId);
    }

    private FollowListResponse followList(List<FollowCursorDto> result, Integer limit, long totalCount) {
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

        return new FollowListResponse(
                data, nextCursor, nextIdAfter, hasNext, totalCount, SortBy.createdAt, SortDirection.DESCENDING);
    }
}
