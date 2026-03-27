package com.ootd.fitme.domain.attribute.repository;

import com.ootd.fitme.domain.attribute.entity.Attribute;

import java.util.List;

public interface AttributeRepositoryCustom {
    List<Attribute> findAttributesWithCondition(String sortBy, String sortDirection, String keywordLike);
}
