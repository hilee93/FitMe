package com.ootd.fitme.domain.clothesattributeselectablevalue.repository;

import com.ootd.fitme.domain.clothesattributeselectablevalue.entity.ClothesAttributeSelectableValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClothesAttributeSelectableValueRepository extends JpaRepository<ClothesAttributeSelectableValue, UUID> {
}
