package com.ootd.fitme.domain.follow.service;

import com.ootd.fitme.domain.follow.dto.request.FollowCreateRequest;
import com.ootd.fitme.domain.follow.entity.Follow;
import com.ootd.fitme.domain.follow.repository.FollowRepository;
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
import static org.mockito.Mockito.never;


@ExtendWith(MockitoExtension.class)
class FollowServiceUnitTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private FollowCountService followCountService;

    @InjectMocks
    private FollowServiceImpl followServiceImpl;

    @Nested
    @DisplayName("팔로우 생성")
    class FollowCreateTest {

        @Test
        @DisplayName("성공 - 중복이 없으면 저장과 팔로우 카운트가 증가한다")
        void createFollow_noDuplication_savesAndIncreasesCount() {

            //given
            UUID followerId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();
            FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);

            given(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId))
                    .willReturn(Optional.empty());

            given(followRepository.save(any(Follow.class)))
                    .willReturn(Follow.create(followerId, followeeId));

            //when
            followServiceImpl.createFollow(request);

            //then
            then(followRepository).should().save(any(Follow.class));
            then(followCountService).should().increaseFollowCount(any(Follow.class));
        }

        @Test
        @DisplayName("실패 - 팔로우 중이면 예외가 발생하고 저장되지 않는다")
        void createFollow_alreadyFollowed_fail() {

            //given
            UUID followerId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();
            FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);

            given(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId))
                    .willReturn(Optional.of(Follow.create(followerId, followeeId)));

            // when & then
            assertThatThrownBy(() -> followServiceImpl.createFollow(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 팔로우한 사용자입니다.");

            then(followRepository).should(never()).save(any(Follow.class));
            then(followCountService).should(never()).increaseFollowCount(any(Follow.class));
        }
    }

    @Nested
    @DisplayName("팔로우 취소")
    class FollowCancelTest {

        @Test
        @DisplayName("성공 - 팔로우가 존재하면 삭제가 호출된다")
        void cancelFollow_success() {

            //given
            UUID followId = UUID.randomUUID();
            Follow follow = Follow.create(UUID.randomUUID(), UUID.randomUUID());

            given(followRepository.findById(followId))
                    .willReturn(Optional.of(follow));

            //when
            followServiceImpl.cancelFollow(followId);

            //then
            then(followCountService).should().decreaseFollowCount(follow);
            then(followRepository).should().deleteById(followId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않은 팔로우를 취소하면 예외가 발생하고 삭제되지 않는다")
        void cancelFollow_fail() {

            //given
            UUID followId = UUID.randomUUID();

            given(followRepository.findById(followId)).willReturn(Optional.empty());

            //when & then
            assertThatThrownBy(() -> followServiceImpl.cancelFollow(followId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 팔로우입니다.");

            then(followCountService).should(never()).decreaseFollowCount(any(Follow.class));
            then(followRepository).should(never()).deleteById(any(UUID.class));
        }
    }
}