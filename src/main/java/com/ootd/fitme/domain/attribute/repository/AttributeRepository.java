package com.ootd.fitme.domain.attribute.repository;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AttributeRepository extends JpaRepository<Attribute, UUID> {
}
