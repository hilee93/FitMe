package com.ootd.fitme.domain.follow.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FollowTest {

    @Test
    @DisplayName("성공 - 서로 다른 ID로 Follow가 생성된다")
    void create_Follow_returnFollow() {

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
    @DisplayName("실패 - 자기 자신을 팔로우하면 예외가 발생한다")
    void create_Follow_throwsException() {

        //given
        UUID sameId = UUID.randomUUID();

        //when & then
        assertThatThrownBy(() -> Follow.create(sameId, sameId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("자신을 팔로우 할 수 없습니다.");
    }

}