package com.ootd.fitme.domain.follow.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FollowTest {

    @Test
    @DisplayName("팔로우를 생성할 수 있다")
    void create_follow_success() {

        //given
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();

        //when
        Follow follow = Follow.create(followerId, followeeId);

        //then
        assertThat(follow.getFollowerId()).isEqualTo(followerId);
        assertThat(follow.getFolloweeId()).isEqualTo(followeeId);
    }

    @Test
    @DisplayName("자기 자신을 팔로우 할 수 없다")
    void create_follow_fail() {

        //given
        UUID sameId = UUID.randomUUID();

        //when & then
        assertThatThrownBy(() -> Follow.create(sameId, sameId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("자신을 팔로우 할 수 없습니다.");
    }

}