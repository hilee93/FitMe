package com.ootd.fitme.domain.follow.repository;

import com.ootd.fitme.domain.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID>, FollowRepositoryCustom {

    // 팔로우 확인 검증
    Optional<Follow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

}
