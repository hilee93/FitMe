package com.ootd.fitme.domain.feedlike.repository;

import com.ootd.fitme.domain.feedlike.entity.FeedLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FeedLikeRepository extends JpaRepository<FeedLike, UUID> {
}
