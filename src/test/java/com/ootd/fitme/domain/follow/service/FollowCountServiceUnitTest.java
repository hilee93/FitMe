package com.ootd.fitme.domain.follow.service;


import com.ootd.fitme.domain.follow.entity.Follow;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class FollowCountServiceUnitTest {

    @Mock
    private ProfileRepository profileRepository;

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
        @DisplayName("성공 - Profile이 있으면 count 메서드가 호출된다")
        void increaseFollowCount_profileExist_callCountMethod(){

            //given
            Profile followerProfile = mock(Profile.class);
            Profile followeeProfile = mock(Profile.class);

            given(profileRepository.findByUserId(followerId)).willReturn(Optional.of(followerProfile));
            given(profileRepository.findByUserId(followeeId)).willReturn(Optional.of(followeeProfile));

            //when
            followCountService.increaseFollowCount(follow);

            //then
            then(followerProfile).should().increaseFolloweeCount();
            then(followeeProfile).should().increaseFollowerCount();
        }

        @Test
        @DisplayName("실패 - Profile이 없으면 예외가 발생한다")
        void increaseFollowCount_profileNotExist_throwException(){

            //given
            given(profileRepository.findByUserId(any())).willReturn(Optional.empty());

            //when & then
            assertThatThrownBy(() -> followCountService.increaseFollowCount(follow))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("프로필을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("팔로우 카운트 감소")
    class DecreaseFollowCountTest{

        @Test
        @DisplayName("성공 - Profile이 있으면 count 메서드가 호출된다")
        void decreaseFollowCount_profileExist_callCountMethod(){

            //given
            Profile followerProfile = mock(Profile.class);
            Profile followeeProfile = mock(Profile.class);

            given(profileRepository.findByUserId(followerId)).willReturn(Optional.of(followerProfile));
            given(profileRepository.findByUserId(followeeId)).willReturn(Optional.of(followeeProfile));

            //when
            followCountService.decreaseFollowCount(follow);

            //then
            then(followerProfile).should().decreaseFolloweeCount();
            then(followeeProfile).should().decreaseFollowerCount();
        }

        @Test
        @DisplayName("실패 - Profile이 없으면 예외가 발생한다")
        void decreaseFollowCount_profileNotExist_throwException(){

            //given
            given(profileRepository.findByUserId(any())).willReturn(Optional.empty());

            //when & then
            assertThatThrownBy(() -> followCountService.decreaseFollowCount(follow))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("프로필을 찾을 수 없습니다.");
        }
    }



}