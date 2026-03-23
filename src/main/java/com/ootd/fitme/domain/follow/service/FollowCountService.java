package com.ootd.fitme.domain.follow.service;

import com.ootd.fitme.domain.follow.entity.Follow;

public interface FollowCountService {

    void increaseFollowCount(Follow follow);
    void decreaseFollowCount(Follow follow);
}
