package com.ootd.fitme.domain.region.repository;

import com.ootd.fitme.domain.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RegionRepository extends JpaRepository<Region, UUID> {
    Optional<Region> findByRegionCode(String regionCode);
}
