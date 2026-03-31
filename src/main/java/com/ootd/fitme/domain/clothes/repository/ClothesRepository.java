package com.ootd.fitme.domain.clothes.repository;

import com.ootd.fitme.domain.clothes.entity.Clothes;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ClothesRepository extends JpaRepository<Clothes, UUID> {
    @EntityGraph(attributePaths = {
            "attributes",
            "attributes.clothesAttributeSelectableValue",
            "attributes.attribute"
    })
    @Query("SELECT c FROM Clothes c WHERE c.id = :id")
    Optional<Clothes> findByIdWithDetails(@Param("id") UUID id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Clothes c WHERE c.id = :id")
    void deleteByIdInBulk(@Param("id") UUID id);
}
