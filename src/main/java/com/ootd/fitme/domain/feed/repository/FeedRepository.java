package com.ootd.fitme.domain.feed.repository;

import com.ootd.fitme.domain.feed.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface FeedRepository extends JpaRepository<Feed, UUID> {
    @Modifying
    @Query("update Feed f set f.likeCount = f.likeCount + 1 where f.id = :feedId")
    int increaseLikeCount(UUID feedId);

    @Modifying
    @Query("update Feed f set f.likeCount = f.likeCount - 1 where f.id = :feedId and f.likeCount > 0")
    int decreaseLike(UUID feedId);
}
