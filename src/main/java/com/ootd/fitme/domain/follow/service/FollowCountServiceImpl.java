package com.ootd.fitme.domain.follow.service;

import com.ootd.fitme.domain.follow.entity.Follow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowCountServiceImpl implements FollowCountService {

    @Override
    public void increaseFollowCount(Follow follow) {
        // TODO : 팔로우 카운트 증가 로직
    }

    @Override
    public void decreaseFollowCount(Follow follow) {
        // TODO : 팔로우 카운트 감소 로직
    }
}
