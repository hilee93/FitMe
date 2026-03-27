package com.ootd.fitme.domain.follow.repository;


import com.ootd.fitme.domain.follow.dto.response.UserSummary;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import com.ootd.fitme.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("local")
@Import({JpaAuditingConfig.class, QuerydslConfig.class, FollowProfileQueryRepositoryImpl.class})
class FollowProfileQueryRepositoryTest {

    @Autowired
    private FollowProfileQueryRepository followProfileQueryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private EntityManager em;

    private UUID userId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.create("test@test.com", "123456"));
        userId = user.getId();
        profileRepository.save(Profile.create(
                "진우", null, null, 0, 0, null, null,
                null, null, null, null, user));
    }

    @Nested
    @DisplayName("팔로워 수 증가")
    class IncreaseFollowerCountTest {

        @Test
        @DisplayName("followerCount가 1 증가한다")
        void increaseFollowerCount_increasesByOne() {

            //when
            followProfileQueryRepository.increaseFollowerCount(userId);
            em.flush();
            em.clear();
            int count = followProfileQueryRepository.findFollowerCountByUserId(userId);

            //then
            assertThat(count).isEqualTo(1);
        }

    }

    @Nested
    @DisplayName("팔로워 수 감소")
    class DecreaseFollowerCountTest {

        @Test
        @DisplayName("followerCount가 1 감소한다")
        void decreaseFollowerCount_decreasesByOne() {

            //given
            followProfileQueryRepository.increaseFollowerCount(userId);
            em.flush();
            em.clear();

            //when
            followProfileQueryRepository.decreaseFollowerCount(userId);
            em.flush();
            em.clear();
            int count = followProfileQueryRepository.findFollowerCountByUserId(userId);

            //then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("followerCount가 0이면 감소하지 않는다")
        void decreaseFollowerCount_countIsZero_notDecrease() {

            //when
            followProfileQueryRepository.decreaseFollowerCount(userId);
            em.flush();
            em.clear();
            int count = followProfileQueryRepository.findFollowerCountByUserId(userId);

            //then
            assertThat(count).isEqualTo(0);

        }
    }

    @Nested
    @DisplayName("팔로잉 수 증가")
    class IncreaseFolloweeCountTest {

        @Test
        @DisplayName("followeeCount가 1 증가한다")
        void increaseFolloweeCount_increasesByOne() {

            //when
            followProfileQueryRepository.increaseFolloweeCount(userId);
            em.flush();
            em.clear();
            int count = followProfileQueryRepository.findFolloweeCountByUserId(userId);

            //then
            assertThat(count).isEqualTo(1);
        }

    }

    @Nested
    @DisplayName("팔로잉 수 감소")
    class DecreaseFolloweeCountTest {

        @Test
        @DisplayName("followeeCount가 1 감소한다")
        void decreaseFolloweeCount_decreasesByOne() {

            //given
            followProfileQueryRepository.increaseFolloweeCount(userId);
            em.flush();
            em.clear();

            //when
            followProfileQueryRepository.decreaseFolloweeCount(userId);
            em.flush();
            em.clear();
            int count = followProfileQueryRepository.findFolloweeCountByUserId(userId);

            //then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("followeeCount가 0이면 감소하지 않는다")
        void decreaseFolloweeCount_countIsZero_notDecrease() {

            //when
            followProfileQueryRepository.decreaseFolloweeCount(userId);
            em.flush();
            em.clear();
            int count = followProfileQueryRepository.findFolloweeCountByUserId(userId);

            //then
            assertThat(count).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("유저 요약 정보 조회")
    class FindUserSummaryTest {

        @Test
        @DisplayName("userId로 name과 profileImageUrl를 조회한다")
        void findUserSummary_returnNameAndProfileImageUrl() {

            //when
            UserSummary summary = followProfileQueryRepository.findUserSummaryByUserId(userId);

            //then
            assertThat(summary.userId()).isEqualTo(userId);
            assertThat(summary.name()).isEqualTo("진우");
            assertThat(summary.profileImageUrl()).isNull();
        }
    }
}