package com.ootd.fitme.domain.catalog.repository;

import com.ootd.fitme.domain.catalog.entity.CatalogClothes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CatalogClothesRepository extends JpaRepository<CatalogClothes, UUID> {
    Optional<CatalogClothes> findByOriginalUrl(String originalUrl);
}
