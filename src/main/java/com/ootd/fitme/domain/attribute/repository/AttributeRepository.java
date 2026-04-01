package com.ootd.fitme.domain.attribute.repository;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AttributeRepository extends JpaRepository<Attribute, UUID>, AttributeRepositoryCustom {
    boolean existsByName(String name);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Attribute a WHERE a.id = :id")
    void deleteByIdInBulk(@Param("id") UUID id);
}
