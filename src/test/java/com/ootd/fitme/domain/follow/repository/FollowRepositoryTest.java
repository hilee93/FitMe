package com.ootd.fitme.domain.follow.repository;

import com.ootd.fitme.domain.follow.dto.response.FollowCursorDto;
import com.ootd.fitme.domain.follow.entity.Follow;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import com.ootd.fitme.global.config.QuerydslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("local")
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
class FollowRepositoryTest {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    private UUID followerId;
    private UUID followeeId;

    @BeforeEach
    void setUp() {
        User follower = userRepository.save(User.create("follower@a.com", "123456"));
        User followee = userRepository.save(User.create("followee@a.com", "123456"));
        followerId = follower.getId();
        followeeId = followee.getId();
    }

    @Test
    @DisplayName("존재하는 팔로우 조회 시 Follow가 반환된다")
    void findByFollowerIdAndFolloweeId_exits_returnFollow() {

        //given
        followRepository.save(Follow.create(followerId, followeeId));

        //when
        Optional<Follow> result = followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().getFollowerId()).isEqualTo(followerId);
        assertThat(result.get().getFolloweeId()).isEqualTo(followeeId);
    }

    @Test
    @DisplayName("존재하지 않은 ID로 조회하면 빈 값으로 반환된다")
    void findByFollowerIdAndFolloweeId_notExits_returnEmpty(){

        //given
        UUID randomId1 = UUID.randomUUID();
        UUID randomId2 = UUID.randomUUID();

        //when
        Optional<Follow> result = followRepository.findByFollowerIdAndFolloweeId(randomId1, randomId2);

        //then
        assertThat(result).isEmpty();
    }

    @Nested
    @DisplayName("팔로잉 목록 조회")
    class FindFollowingsTest {

        private UUID follower1Id;
        private UUID followee1Id;
        private UUID followee2Id;
        private UUID followee3Id;

        @BeforeEach
        void setUp() {
            User follower1 = userRepository.save(User.create("follower1@test.com", "123456"));
            User followee1 = userRepository.save(User.create("followee1@test.com", "123456"));
            User followee2 = userRepository.save(User.create("followee2@test.com", "123456"));
            User followee3 = userRepository.save(User.create("followee3@test.com", "123456"));

            saveProfile(follower1, "아토");
            saveProfile(followee1, "김진우");
            saveProfile(followee2, "이영희");
            saveProfile(followee3,"김철수");

            follower1Id = follower1.getId();
            followee1Id = followee1.getId();
            followee2Id = followee2.getId();
            followee3Id = followee3.getId();
        }

        @Test
        @DisplayName("followerId로 팔로잉 목록을 조회한다")
        void findFollowings_followerId_returnList() {

            //given
            followRepository.save(Follow.create(follower1Id, followee1Id));
            followRepository.save(Follow.create(follower1Id, followee2Id));

            //when
            List<FollowCursorDto> followings = followRepository.findFollowings(
                    follower1Id, null, null, 2, null);

            //then
            assertThat(followings.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("limit+1개를 조회한다")
        void findFollowings_return_limitPlusOne(){

            //given
            followRepository.save(Follow.create(follower1Id, followee1Id));
            followRepository.save(Follow.create(follower1Id, followee2Id));
            followRepository.save(Follow.create(follower1Id, followee3Id));

            //when
            List<FollowCursorDto> followings = followRepository.findFollowings(
                    follower1Id, null, null, 2, null);

            //then
            assertThat(followings.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("nameLike로 이름 검색이 된다")
        void findFollowings_search_nameLike() {

            //given
            followRepository.save(Follow.create(follower1Id, followee1Id));
            followRepository.save(Follow.create(follower1Id, followee2Id));
            followRepository.save(Follow.create(follower1Id, followee3Id));

            //when
            List<FollowCursorDto> followings = followRepository.findFollowings(
                    follower1Id, null, null, 3, "김");

            //then
            assertThat(followings.size()).isEqualTo(2);
            assertThat(followings).extracting(f -> f.followee().name())
                    .allMatch(name -> name.contains("김"));
        }

        @Test
        @DisplayName("cursor로 다음 페이지를 조회한다")
        void findFollowings_cursor_returnNextPage() {

            //given
            followRepository.save(Follow.create(follower1Id, followee1Id));
            followRepository.save(Follow.create(follower1Id, followee2Id));
            followRepository.save(Follow.create(follower1Id, followee3Id));

            List<FollowCursorDto> firstPage = followRepository.findFollowings(
                    follower1Id, null, null, 2, null);

            String cursor = firstPage.get(1).createdAt().toString();
            UUID idAfter = firstPage.get(1).id();

            //when
            List<FollowCursorDto> secondPage = followRepository.findFollowings(
                    follower1Id, cursor, idAfter, 2, null);

            //then
            assertThat(secondPage.size()).isEqualTo(1);
        }
    }
    @Nested
    @DisplayName("팔로우 목록 조회")
    class FindFollowersTest {

        private UUID followee1Id;
        private UUID follower1Id;
        private UUID follower2Id;
        private UUID follower3Id;

        @BeforeEach
        void setUp() {
            User followee1 = userRepository.save(User.create("followee1@b.com", "123456"));
            User follower1 = userRepository.save(User.create("follower1@b.com", "123456"));
            User follower2 = userRepository.save(User.create("follower2@b.com", "123456"));
            User follower3 = userRepository.save(User.create("follower3@b.com", "123456"));

            saveProfile(followee1, "아토");
            saveProfile(follower1, "김진우");
            saveProfile(follower2, "이영희");
            saveProfile(follower3,"김철수");

            followee1Id = followee1.getId();
            follower1Id = follower1.getId();
            follower2Id = follower2.getId();
            follower3Id = follower3.getId();
        }

        @Test
        @DisplayName("followeeId로 팔로워 목록을 조회한다")
        void findFollowers_followeeId_returnList() {

            //given
            followRepository.save(Follow.create(follower1Id, followee1Id));
            followRepository.save(Follow.create(follower2Id, followee1Id));

            //when
            List<FollowCursorDto> followers = followRepository.findFollowers(
                    followee1Id, null, null, 2, null);

            //then
            assertThat(followers.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("팔로워 수 조회")
    class CountFollowersTest {

        private UUID followeeId;
        private UUID follower1Id;
        private UUID follower2Id;
        private UUID follower3Id;

        @BeforeEach
        void setUp() {
            User followee = userRepository.save(User.create("followee@c.com", "123456"));
            User follower1 = userRepository.save(User.create("follower1@c.com", "123456"));
            User follower2 = userRepository.save(User.create("follower2@c.com", "123456"));
            User follower3 = userRepository.save(User.create("follower3@c.com", "123456"));

            saveProfile(followee, "아토");
            saveProfile(follower1, "김진우");
            saveProfile(follower2, "이영희");
            saveProfile(follower3,"김철수");

            followeeId = followee.getId();
            follower1Id = follower1.getId();
            follower2Id = follower2.getId();
            follower3Id = follower3.getId();
        }

        @Test
        @DisplayName("nameLike가 있으면 필터링 된 팔로워 수를 반환한다")
        void countFollowers_nameLike_returnFilteredCount() {

            //given
            followRepository.save(Follow.create(follower1Id, followeeId));
            followRepository.save(Follow.create(follower2Id, followeeId));
            followRepository.save(Follow.create(follower3Id, followeeId));

            //when
            long count = followRepository.countFollowers(followeeId, "김");

            //then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("nameLike가 없으면 전체 팔로워 수를 반환한다")
        void countFollowers_nameLike_returnAllCount() {

            //given
            followRepository.save(Follow.create(follower1Id, followeeId));
            followRepository.save(Follow.create(follower2Id, followeeId));
            followRepository.save(Follow.create(follower3Id, followeeId));

            //when
            long count = followRepository.countFollowers(followeeId, null);

            //then
            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("팔로잉 수 조회")
    class CountFollowingsTest {

        private UUID followerId;
        private UUID followee1Id;
        private UUID followee2Id;
        private UUID followee3Id;

        @BeforeEach
        void setUp() {
            User follower = userRepository.save(User.create("follower@d.com", "123456"));
            User followee1 = userRepository.save(User.create("followee1@d.com", "123456"));
            User followee2 = userRepository.save(User.create("followee2@d.com", "123456"));
            User followee3 = userRepository.save(User.create("followee3@d.com", "123456"));

            saveProfile(follower, "아토");
            saveProfile(followee1, "김진우");
            saveProfile(followee2, "이영희");
            saveProfile(followee3,"김철수");

            followerId = follower.getId();
            followee1Id = followee1.getId();
            followee2Id = followee2.getId();
            followee3Id = followee3.getId();
        }

        @Test
        @DisplayName("nameLike가 있으면 필터링된 팔로잉 수를 반환한다")
        void countFollowings_nameLike_returnFilteredCount() {

            //given
            followRepository.save(Follow.create(followerId, followee1Id));
            followRepository.save(Follow.create(followerId, followee2Id));
            followRepository.save(Follow.create(followerId, followee3Id));

            //when
            long count = followRepository.countFollowings(followerId, "김");

            //then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("nameLike가 없으면 전체 팔로잉 수를 반환한다")
        void countFollowings_nameLike_returnAllCount() {

            //given
            followRepository.save(Follow.create(followerId, followee1Id));
            followRepository.save(Follow.create(followerId, followee2Id));
            followRepository.save(Follow.create(followerId, followee3Id));

            //when
            long count = followRepository.countFollowings(followerId, null);

            //then
            assertThat(count).isEqualTo(3);
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

