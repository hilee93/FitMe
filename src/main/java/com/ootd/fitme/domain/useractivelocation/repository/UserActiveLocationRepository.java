package com.ootd.fitme.domain.useractivelocation.repository;

import com.ootd.fitme.domain.useractivelocation.entity.UserActiveLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserActiveLocationRepository extends JpaRepository<UserActiveLocation, UUID> {
    Optional<UserActiveLocation> findByUserId(UUID userId);

    @Query("""
        select ual.user.id
        from UserActiveLocation ual
        where ual.region.id = :regionId
        """)
    List<UUID> findUserIdsByRegionId(UUID regionId);
}
