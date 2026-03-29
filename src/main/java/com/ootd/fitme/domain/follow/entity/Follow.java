package com.ootd.fitme.domain.follow.entity;


import com.ootd.fitme.domain.base.BaseEntity;
import com.ootd.fitme.domain.follow.exception.FollowSelfNotAllowedException;
import com.ootd.fitme.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "follows")
public class Follow extends BaseEntity {

    @Column(name = "follower_id", nullable = false)
    private UUID followerId;

    @Column(name = "followee_id", nullable = false)
    private UUID followeeId;

    private Follow(UUID followerId, UUID followeeId) {
        this.followerId = followerId;
        this.followeeId = followeeId;
    }

    public static Follow create(UUID followerId, UUID followeeId) {
        if (followerId.equals(followeeId)) {
            throw new FollowSelfNotAllowedException(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }
        return new Follow(followerId, followeeId);
    }
}
