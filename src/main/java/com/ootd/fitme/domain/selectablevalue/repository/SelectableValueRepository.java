package com.ootd.fitme.domain.selectablevalue.repository;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SelectableValueRepository extends JpaRepository<SelectableValue, UUID> {
    Optional<SelectableValue> findByAttributeAndType(Attribute attribute, String type);

    List<SelectableValue> findAllByAttributeIdIn(List<UUID> definitionIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SelectableValue s WHERE s.id = :id")
    void deleteByIdInBulk(@Param("id") UUID id);
}
