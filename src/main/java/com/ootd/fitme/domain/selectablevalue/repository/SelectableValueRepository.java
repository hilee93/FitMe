package com.ootd.fitme.domain.selectablevalue.repository;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SelectableValueRepository extends JpaRepository<SelectableValue, UUID> {
}
