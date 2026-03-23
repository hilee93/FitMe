package com.ootd.fitme.domain.follow.service;

import com.ootd.fitme.domain.follow.dto.request.FollowCreateRequest;
import com.ootd.fitme.domain.follow.dto.response.FollowDto;
import com.ootd.fitme.domain.follow.dto.response.UserSummary;
import com.ootd.fitme.domain.follow.entity.Follow;
import com.ootd.fitme.domain.follow.repository.FollowRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;

    @Override
    @Transactional
    public FollowDto createFollow(FollowCreateRequest request) {

        // todo : Exception 수정하기
        if(request.followerId().equals(request.followeeId())) {
            throw new IllegalArgumentException("자기 자신을 팔로우 할 수 없습니다.");
        }
        // todo : Exception 수정하기
        if(followRepository.findByFollowerIdAndFolloweeId(request.followerId(), request.followeeId()).isPresent()) {
            throw new IllegalArgumentException("이미 팔로우 했습니다.");
        }

        Follow follow = Follow.create(request.followerId(), request.followeeId());
        Follow saveFollow = followRepository.save(follow);

        // todo : User 레포지토리 가져오기
        UserSummary follower = new UserSummary(saveFollow.getFollowerId(), null, null);
        UserSummary followee = new UserSummary(saveFollow.getFolloweeId(), null, null);
        return new FollowDto(saveFollow.getId(), follower, followee);

    }
}
