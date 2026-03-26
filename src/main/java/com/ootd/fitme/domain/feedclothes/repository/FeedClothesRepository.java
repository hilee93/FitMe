package com.ootd.fitme.domain.feedclothes.repository;

import com.ootd.fitme.domain.feedclothes.entity.FeedClothes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FeedClothesRepository extends JpaRepository<FeedClothes, UUID> {
}
