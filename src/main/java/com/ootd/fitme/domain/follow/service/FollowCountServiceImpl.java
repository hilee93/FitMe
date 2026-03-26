package com.ootd.fitme.domain.follow.service;

import com.ootd.fitme.domain.follow.entity.Follow;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowCountServiceImpl implements FollowCountService {

    private final ProfileRepository profileRepository;

    @Override
    @Transactional
    public void increaseFollowCount(Follow follow) {
        Profile followerProfile = getProfile(follow.getFollowerId());
        followerProfile.increaseFolloweeCount();

        Profile followeeProfile = getProfile(follow.getFolloweeId());
        followeeProfile.increaseFollowerCount();
    }

    @Override
    @Transactional
    public void decreaseFollowCount(Follow follow) {
        Profile followerProfile =  getProfile(follow.getFollowerId());
        followerProfile.decreaseFolloweeCount();

        Profile followeeProfile = getProfile(follow.getFolloweeId());
        followeeProfile.decreaseFollowerCount();
    }

    // TODO : 커스텀 예외 교체
    private Profile getProfile(UUID userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));
    }
}
