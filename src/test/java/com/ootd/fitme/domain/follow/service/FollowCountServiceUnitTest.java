package com.ootd.fitme.domain.follow.service;


import com.ootd.fitme.domain.follow.entity.Follow;
import com.ootd.fitme.domain.follow.repository.FollowProfileQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class FollowCountServiceUnitTest {

    @Mock
    private FollowProfileQueryRepository followProfileQueryRepository;

    @InjectMocks
    private FollowCountServiceImpl followCountService;

    private final UUID followerId = UUID.randomUUID();
    private final UUID followeeId = UUID.randomUUID();
    private Follow follow;

    @BeforeEach
    void setUp(){
        follow = Follow.create(followerId, followeeId);
    }

    @Nested
    @DisplayName("팔로우 카운트 증가")
    class IncreaseFollowCountTest{

        @Test
        @DisplayName("성공 - increaseCount 메서드가 호출된다")
        void increaseFollowCount_profileExist_callCountMethod(){

            //when
            followCountService.increaseFollowCount(follow);

            //then
            then(followProfileQueryRepository).should().increaseFollowerCount(followeeId);
            then(followProfileQueryRepository).should().increaseFolloweeCount(followerId);
        }
    }

    @Nested
    @DisplayName("팔로우 카운트 감소")
    class DecreaseFollowCountTest{

        @Test
        @DisplayName("성공 - decreaseCount 메서드가 호출된다")
        void decreaseFollowCount_profileExist_callCountMethod(){

            //when
            followCountService.decreaseFollowCount(follow);

            //then
            then(followProfileQueryRepository).should().decreaseFollowerCount(followeeId);
            then(followProfileQueryRepository).should().decreaseFolloweeCount(followerId);
        }
    }
}