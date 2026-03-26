package com.ootd.fitme.domain.feedlike.repository;

import com.ootd.fitme.domain.feedlike.entity.FeedLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FeedLikeRepository extends JpaRepository<FeedLike, UUID> {
    Optional<FeedLike> findByFeedIdAndUserId(UUID feedId, UUID userId);

    boolean existsByFeedIdAndUserId(UUID feedId, UUID userId);
}
