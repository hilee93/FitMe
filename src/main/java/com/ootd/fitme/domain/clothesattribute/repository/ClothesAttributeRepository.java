package com.ootd.fitme.domain.clothesattribute.repository;

import com.ootd.fitme.domain.clothesattribute.entity.ClothesAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClothesAttributeRepository extends JpaRepository<ClothesAttribute, UUID> {
}
