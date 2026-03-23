package com.ootd.fitme.domain.follow.service;

import com.ootd.fitme.domain.follow.dto.request.FollowCreateRequest;
import com.ootd.fitme.domain.follow.dto.response.FollowDto;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;


@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private FollowServiceImpl followServiceImpl;

    @Nested
    @DisplayName("팔로우 생성")
    class FollowCreateTest {

        @Test
        @DisplayName("팔로우를 할 수 있다")
        void createFollow_success() {

            //given
            UUID followerId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();
            FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);

            given(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId))
                    .willReturn(Optional.empty());

            given(followRepository.save(any()))
                    .willReturn(Follow.create(followerId, followeeId));

            //when
            FollowDto result = followServiceImpl.createFollow(request);

            //then
            assertThat(result).isNotNull();
            assertThat(result.follower().userId()).isEqualTo(followerId);
            assertThat(result.followee().userId()).isEqualTo(followeeId);
            then(followRepository).should().save(any());
        }

        @Test
        @DisplayName("자기 자신을 팔로우 할 수 없다")
        void createFollow_selfFollow_fail() {

            //given
            UUID userId = UUID.randomUUID();
            FollowCreateRequest request = new FollowCreateRequest(userId, userId);

            //when & then
            assertThatThrownBy(() -> followServiceImpl.createFollow(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("자신을 팔로우 할 수 없습니다.");
        }

        @Test
        @DisplayName("이미 팔로우한 사용자를 다시 팔로우 할 수 없다")
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
        }
    }

    @Nested
    @DisplayName("팔로우 취소")
    class FollowCancelTest {

        @Test
        @DisplayName("팔로우 취소 할 수 있다")
        void cancelFollow_success() {

            //given
            UUID followerId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();
            Follow follow = Follow.create(followerId, followeeId);

            given(followRepository.findById(followerId))
                    .willReturn(Optional.of(follow));

            //when
            followServiceImpl.cancelFollow(followerId);

            //then
            then(followRepository).should().deleteById(followerId);
        }

        @Test
        @DisplayName("존재하지 않는 팔로우는 취소할 수 없다")
        void cancelFollow_fail() {

            //given
            UUID followId = UUID.randomUUID();

            given(followRepository.findById(followId)).willReturn(Optional.empty());

            //when & then
            assertThatThrownBy(() -> followServiceImpl.cancelFollow(followId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 팔로우입니다.");
        }
    }
}