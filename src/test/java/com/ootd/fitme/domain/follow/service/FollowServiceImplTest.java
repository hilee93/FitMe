package com.ootd.fitme.domain.follow.service;

import com.ootd.fitme.domain.follow.dto.request.FollowCreateRequest;
import com.ootd.fitme.domain.follow.dto.response.FollowDto;
import com.ootd.fitme.domain.follow.dto.response.FollowListResponse;
import com.ootd.fitme.domain.follow.dto.response.FollowSummaryDto;
import com.ootd.fitme.domain.follow.entity.Follow;
import com.ootd.fitme.domain.follow.exception.FollowAlreadyExistsException;
import com.ootd.fitme.domain.follow.exception.FollowNotFoundException;
import com.ootd.fitme.domain.follow.repository.FollowRepository;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
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

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private EntityManager em;

    private UUID followerId;
    private UUID followeeId;

    @BeforeEach
    void setUp() {
        User follower = userRepository.save(User.create("follower@a.com", "123456"));
        User followee = userRepository.save(User.create("followee@a.com", "123456"));
        followerId = follower.getId();
        followeeId = followee.getId();

        saveProfile(follower, "팔로워");
        saveProfile(followee, "팔로잉");
    }

    @Nested
    @DisplayName("팔로우 생성")
    class FollowCreateTest {

        @Test
        @DisplayName("성공 - 중복이 없으면 follow가 DB에 저장되고 카운트가 증가한다")
        void createFollow_noDuplicate_saveDBAndIncreaseCount() {

            //given
            FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);

            //when
            followServiceImpl.createFollow(request);
            em.flush();
            em.clear();
            Profile followerProfile = profileRepository.findByUserId(followerId).get();
            Profile followeeProfile = profileRepository.findByUserId(followeeId).get();

            //then
            assertThat(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)).isPresent();
            assertThat(followerProfile.getFolloweeCount()).isEqualTo(1);
            assertThat(followeeProfile.getFollowerCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("실패 - 팔로우 상태면 예외가 발생하고 DB에 저장하지 않는다")
        void createFollow_alreadyFollowed_throwExceptionOrNotSavedDB() {

            //given
            FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);
            followServiceImpl.createFollow(request);
            em.flush();
            em.clear();

            //when & then
            assertThatThrownBy(() -> followServiceImpl.createFollow(request))
                    .isInstanceOf(FollowAlreadyExistsException.class)
                    .hasMessage("이미 팔로우한 사용자입니다.");

            Profile followerProfile = profileRepository.findByUserId(followerId).get();
            Profile followeeProfile = profileRepository.findByUserId(followeeId).get();

            assertThat(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)).isPresent();
            assertThat(followRepository.findAll()).hasSize(1);
            assertThat(followerProfile.getFolloweeCount()).isEqualTo(1);
            assertThat(followeeProfile.getFollowerCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("성공 - 팔로우 생성시 follower와 followee 정보를 반환한다")
        void createFollow_returnFollowDto() {

            //given
            FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);

            //when
            FollowDto follow = followServiceImpl.createFollow(request);

            //then
            assertThat(follow.follower().userId()).isEqualTo(followerId);
            assertThat(follow.follower().name()).isEqualTo("팔로워");
            assertThat(follow.follower().profileImageUrl()).isNull();
            assertThat(follow.followee().userId()).isEqualTo(followeeId);
            assertThat(follow.followee().name()).isEqualTo("팔로잉");
            assertThat(follow.follower().profileImageUrl()).isNull();

        }
    }

    @Nested
    @DisplayName("팔로우 취소")
    class FollowCancelTest {

        @Test
        @DisplayName("성공 - 팔로우가 존재하면 DB에서 삭제되고 카운트가 감소한다")
        void cancelFollow_existFollow_deletedDBAndDecreaseCount() {

            //given
            FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);
            FollowDto follow = followServiceImpl.createFollow(request);
            em.flush();
            em.clear();

            //when
            followServiceImpl.cancelFollow(follow.id());
            em.flush();
            em.clear();

            Profile followerProfile = profileRepository.findByUserId(followerId).get();
            Profile followeeProfile = profileRepository.findByUserId(followeeId).get();

            //then
            assertThat(followRepository.findById(follow.id())).isEmpty();
            assertThat(followerProfile.getFolloweeCount()).isEqualTo(0);
            assertThat(followeeProfile.getFollowerCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 팔로우를 취소하면 예외가 발생한다")
        void cancelFollow_notExistFollow_throwException() {

            //given
            UUID randomId = UUID.randomUUID();

            //when & then
            assertThatThrownBy(() -> followServiceImpl.cancelFollow(randomId))
                    .isInstanceOf(FollowNotFoundException.class)
                    .hasMessage("존재하지 않는 팔로우입니다.");

            Profile followerProfile = profileRepository.findByUserId(followerId).get();
            Profile followeeProfile = profileRepository.findByUserId(followeeId).get();

            assertThat(followRepository.findById(randomId)).isEmpty();
            assertThat(followerProfile.getFolloweeCount()).isEqualTo(0);
            assertThat(followeeProfile.getFollowerCount()).isEqualTo(0);
        }

    }

    @Nested
    @DisplayName("목록 조회")
    class GetFollowsListTest {

        private UUID follower1Id;
        private UUID follower2Id;
        private UUID followee1Id;
        private UUID followee2Id;
        private UUID followee3Id;
        private UUID followee4Id;

        @BeforeEach
        void setUp() {
            User follower1 = userRepository.save(User.create("follower1@test.com", "123456"));
            User follower2 = userRepository.save(User.create("follower2@test.com", "123456"));
            User followee1 = userRepository.save(User.create("followee1@test.com", "123456"));
            User followee2 = userRepository.save(User.create("followee2@test.com", "123456"));
            User followee3 = userRepository.save(User.create("followee3@test.com", "123456"));
            User followee4 = userRepository.save(User.create("followee4@test.com", "123456"));

            saveProfile(follower1, "이형일");
            saveProfile(follower2, "최현석");
            saveProfile(followee1, "조성연");
            saveProfile(followee2, "김진우");
            saveProfile(followee3, "신제원");
            saveProfile(followee4, "김태언");


            follower1Id = follower1.getId();
            follower2Id = follower2.getId();
            followee1Id = followee1.getId();
            followee2Id = followee2.getId();
            followee3Id = followee3.getId();
            followee4Id = followee4.getId();
        }

        @Test
        @DisplayName("성공 - 데이터가 limit보다 많으면 hasNext가 true이다")
        void getFollows_sizeOverLimit_hasNextTrue(){

            //given
            followRepository.save(Follow.create(follower1Id, followee1Id));
            followRepository.save(Follow.create(follower1Id, followee2Id));
            followRepository.save(Follow.create(follower1Id, followee3Id));

            //when
            FollowListResponse followings = followServiceImpl.getFollowings(
                    follower1Id, null, null, 2, null);

            //then
            assertThat(followings.hasNext()).isTrue();
            assertThat(followings.data().size()).isEqualTo(2);
        }

        @Test
        @DisplayName("성공 - 데이터가 limit보다 적으면 hasNext가 false이다")
        void getFollows_sizeUnderLimit_hasNextFalse(){

            //given
            followRepository.save(Follow.create(follower1Id, followee1Id));

            //when
            FollowListResponse followings = followServiceImpl.getFollowings(
                    follower1Id, null, null, 1, null);

            //then
            assertThat(followings.hasNext()).isFalse();
            assertThat(followings.data().size()).isEqualTo(1);
        }

        @Test
        @DisplayName("성공 - 데이터가 0개면 빈 리스트를 반환한다")
        void getFollows_dataEmpty_returnList(){

            //when
            FollowListResponse followings = followServiceImpl.getFollowings(
                    follower1Id, null, null, 2, null);

            //then
            assertThat(followings.data()).isEmpty();
            assertThat(followings.hasNext()).isFalse();
        }

        @Test
        @DisplayName("성공 - hasNext가 true이면 nextCursor가 null 아니다")
        void getFollows_hasNextTrue_nextCursorNotNull() {

            //given
            followRepository.save(Follow.create(follower1Id, followee1Id));
            followRepository.save(Follow.create(follower1Id, followee2Id));
            followRepository.save(Follow.create(follower1Id, followee3Id));

            //when
            FollowListResponse followings = followServiceImpl.getFollowings(
                    follower1Id, null, null, 1, null);

            //then
            assertThat(followings.hasNext()).isTrue();
            assertThat(followings.nextCursor()).isNotNull();
            assertThat(followings.nextIdAfter()).isNotNull();
        }

        @Test
        @DisplayName("성공 - hasNext가 false면 nextCursor가 null이다")
        void getFollows_hasNextFalse_nextCursorNull() {

            //given
            followRepository.save(Follow.create(follower1Id, followee1Id));

            //when
            FollowListResponse followings = followServiceImpl.getFollowings(
                    follower1Id, null, null, 2, null);

            //then
            assertThat(followings.hasNext()).isFalse();
            assertThat(followings.nextCursor()).isNull();
            assertThat(followings.nextIdAfter()).isNull();
        }

        @Test
        @DisplayName("성공 - followeeId로 팔로워 목록을 조회한다")
        void getFollowers_followeeId_returnList() {

            //given
            followRepository.save(Follow.create(follower1Id, followee1Id));
            followRepository.save(Follow.create(follower2Id, followee1Id));

            //when
            FollowListResponse followers = followServiceImpl.getFollowers(
                    followee1Id, null, null, 2, null);

            //then
            assertThat(followers.data().size()).isEqualTo(2);
            assertThat(followers.hasNext()).isFalse();
        }

        @Test
        @DisplayName("성공 - nameLike가 있으면 필터링된 totalCount가 반환된다")
        void getFollows_nameLike_returnFilteredCount() {

            //given
            followRepository.save(Follow.create(follower1Id, followee1Id));
            followRepository.save(Follow.create(follower1Id, followee2Id));
            followRepository.save(Follow.create(follower1Id, followee3Id));
            followRepository.save(Follow.create(follower1Id, followee4Id));

            //when
            FollowListResponse result = followServiceImpl.getFollowings(
                    follower1Id, null, null, 10, "김");

            //then
            assertThat(result.totalCount()).isEqualTo(2);

        }
    }

    @Nested
    @DisplayName("팔로우 요약 조회")
    class GetFollowSummaryTest {

        @Test
        @DisplayName("성공 - 내가 팔로우 중이면 followedByMe는 true이다")
        void getFollowSummary_following_followedByMeTrue() {

            //given
            Follow follow = followRepository.save(Follow.create(followerId, followeeId));

            //when
            FollowSummaryDto followSummary = followServiceImpl.getFollowSummary(followeeId, followerId);

            //then
            assertThat(followSummary.followedByMe()).isTrue();
            assertThat(followSummary.followedByMeId()).isEqualTo(follow.getId());
        }

        @Test
        @DisplayName("성공 - 내가 팔로우 안 한 상태면 followedByMe는 false이다")
        void getFollowSummary_notFollowing_followedByMeFalse() {

            //when
            FollowSummaryDto followSummary = followServiceImpl.getFollowSummary(followeeId, followerId);

            //then
            assertThat(followSummary.followedByMe()).isFalse();
            assertThat(followSummary.followedByMeId()).isNull();
        }

        @Test
        @DisplayName("성공 - 상대방이 나를 팔로우하면 followingMe는 true이다")
        void getFollowSummary_followeeFollowingMe_followingMeTrue() {

            //given
            followRepository.save(Follow.create(followeeId, followerId));

            //when
            FollowSummaryDto followSummary = followServiceImpl.getFollowSummary(followeeId, followerId);

            //then
            assertThat(followSummary.followingMe()).isTrue();
        }

        @Test
        @DisplayName("성공 - 상대방이 나를 팔로우를 안하면 followingMe는 false이다")
        void getFollowSummary_followeeNotFollowingMe_followingMeFalse() {

            //when
            FollowSummaryDto followSummary = followServiceImpl.getFollowSummary(followeeId, followerId);

            //then
            assertThat(followSummary.followingMe()).isFalse();
        }

        @Test
        @DisplayName("성공 - 팔로우하면 followerCount와 followingCount가 반환된다")
        void getFollowSummary_follow_followerAndFollowingCountIncrease() {

            //given
            FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);
            followServiceImpl.createFollow(request);

            //when
            FollowSummaryDto followSummary = followServiceImpl.getFollowSummary(followeeId, followerId);

            //then
            assertThat(followSummary.followerCount()).isEqualTo(1);
            assertThat(followSummary.followingCount()).isEqualTo(0);
        }
    }

    private void saveProfile(User user, String name) {
        profileRepository.save(Profile.create(
                name, null, null, 0, 0,
                null, null, null,
                null, null, null,
                user));
    }

}