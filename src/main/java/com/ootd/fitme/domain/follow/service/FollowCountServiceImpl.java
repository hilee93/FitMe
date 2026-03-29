package com.ootd.fitme.domain.follow.service;

import com.ootd.fitme.domain.follow.entity.Follow;
import com.ootd.fitme.domain.follow.repository.FollowProfileQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class FollowCountServiceImpl implements FollowCountService {

    private final FollowProfileQueryRepository followProfileQueryRepository;

    @Override
    @Transactional
    public void increaseFollowCount(Follow follow) {
        followProfileQueryRepository.increaseFollowerCount(follow.getFolloweeId());
        followProfileQueryRepository.increaseFolloweeCount(follow.getFollowerId());
    }

    @Override
    @Transactional
    public void decreaseFollowCount(Follow follow) {
        followProfileQueryRepository.decreaseFollowerCount(follow.getFolloweeId());
        followProfileQueryRepository.decreaseFolloweeCount(follow.getFollowerId());
    }
}
