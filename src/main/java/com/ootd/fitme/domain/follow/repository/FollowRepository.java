package com.ootd.fitme.domain.follow.repository;

import com.ootd.fitme.domain.follow.entity.Follow;
import com.ootd.fitme.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    // 팔로우 확인 검증
    Optional<Follow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    // 특정 사용자를 팔로우하고 있는 사용자 UUID 조회
    @Query("""
    select f.followerId
    from Follow f
    where f.followeeId = :followeeId
    """)
    List<UUID> findFollowerIdsByFolloweeId(UUID followeeId);
}
