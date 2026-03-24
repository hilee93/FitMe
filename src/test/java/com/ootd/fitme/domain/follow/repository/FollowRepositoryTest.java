package com.ootd.fitme.domain.follow.repository;

import com.ootd.fitme.domain.follow.entity.Follow;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("local")
@Import(JpaAuditingConfig.class)
class FollowRepositoryTest {

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

}