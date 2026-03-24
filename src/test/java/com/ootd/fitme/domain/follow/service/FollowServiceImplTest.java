package com.ootd.fitme.domain.follow.service;

import com.ootd.fitme.domain.follow.dto.request.FollowCreateRequest;
import com.ootd.fitme.domain.follow.dto.response.FollowDto;
import com.ootd.fitme.domain.follow.repository.FollowRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class FollowServiceImplTest {

    @Autowired
    private FollowServiceImpl followServiceImpl;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID followerId;
    private UUID followeeId;

    @BeforeEach
    void setUp() {
        User follower = userRepository.save(User.create("follower@a.com", "123456"));
        User followee = userRepository.save(User.create("followee@a.com", "123456"));
        followerId = follower.getId();
        followeeId = followee.getId();
    }

    @Nested
    @DisplayName("팔로우 생성")
    class FollowCreateTest {

        @Test
        @DisplayName("성공 - 중복이 없으면 follow가 DB에 저장된다")
        void createFollow_noDuplicate_saveDB() {

            //given
            FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);

            //when
            followServiceImpl.createFollow(request);

            //then
            assertThat(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)).isPresent();
        }

        @Test
        @DisplayName("실패 - 팔로우 상태면 예외가 발생하고 DB에 저장하지 않는다")
        void createFollow_alreadyFollowed_throwExceptionOrNotSavedDB() {

            //given
            FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);
            followServiceImpl.createFollow(request);

            //when & then
            assertThatThrownBy(() -> followServiceImpl.createFollow(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 팔로우한 사용자입니다.");

            assertThat(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)).isPresent();
            assertThat(followRepository.findAll()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("팔로우 취소")
    class FollowCancelTest {

        @Test
        @DisplayName("성공 - 팔로우가 존재하면 DB에서 삭제된다")
        void cancelFollow_existFollow_deletedDB() {

            //given
            FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);
            FollowDto follow = followServiceImpl.createFollow(request);

            //when
            followServiceImpl.cancelFollow(follow.id());

            //then
            assertThat(followRepository.findById(follow.id())).isEmpty();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 팔로우를 취소하면 예외가 발생한다")
        void cancelFollow_notExistFollow_throwException() {

            //given
            UUID randomId = UUID.randomUUID();

            //when & then
            assertThatThrownBy(() -> followServiceImpl.cancelFollow(randomId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 팔로우입니다.");
        }

    }

}